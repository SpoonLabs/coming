/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>FastDateParser is a fast and thread-safe version of
 * {@link java.text.SimpleDateFormat}.</p>
 *
 * <p>This class can be used as a direct replacement for
 * <code>SimpleDateFormat</code> in most parsing situations.
 * This class is especially useful in multi-threaded server environments.
 * <code>SimpleDateFormat</code> is not thread-safe in any JDK version,
 * nor will it be as Sun have closed the
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4228335">bug</a>/RFE.
 * </p>
 *
 * <p>Only parsing is supported, but all patterns are compatible with
 * SimpleDateFormat.</p>
 *
 * <p>Timing tests indicate this class is as about as fast as SimpleDateFormat
 * in single thread applications and about 25% faster in multi-thread applications.</p>
 *
 * <p>Note that the code only handles Gregorian calendars. The following non-Gregorian
 * calendars use SimpleDateFormat internally, and so will be slower:
 * <ul>
 * <li>ja_JP_TH - Japanese Imperial</li>
 * <li>th_TH (any variant) - Thai Buddhist</li>
 * </ul>
 * </p>
 * @since 3.2
 */
public class FastDateParser implements DateParser, Serializable {
    /**
     * Required for serialization support.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    private static final ConcurrentMap<Locale,TimeZoneStrategy> tzsCache=
        new ConcurrentHashMap<Locale,TimeZoneStrategy>(3);

    static final Locale JAPANESE_IMPERIAL = new Locale("ja","JP","JP");

    // defining fields
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;

    // derived fields
    private transient Pattern parsePattern;
    private transient Strategy[] strategies;
    private transient int thisYear;
    private transient ConcurrentMap<Integer, KeyValue[]> nameValues;

    // dynamic fields to communicate with Strategy
    private transient String currentFormatField;
    private transient Strategy nextStrategy;

    /**
     * <p>Constructs a new FastDateParser.</p>
     *
     * @param pattern non-null {@link java.text.SimpleDateFormat} compatible
     *  pattern
     * @param timeZone non-null time zone to use
     * @param locale non-null locale
     */
    protected FastDateParser(String pattern, TimeZone timeZone, Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
        init();
    }

    /**
     * Initialize derived fields from defining fields.
     * This is called from constructor and from readObject (de-serialization)
     */
    private void init() {
        thisYear= Calendar.getInstance(timeZone, locale).get(Calendar.YEAR);

        nameValues= new ConcurrentHashMap<Integer, KeyValue[]>();

        StringBuilder regex= new StringBuilder();
        List<Strategy> collector = new ArrayList<Strategy>();

        Matcher patternMatcher= formatPattern.matcher(pattern);
        if(!patternMatcher.lookingAt()) {
            throw new IllegalArgumentException("Invalid pattern");
        }

        currentFormatField= patternMatcher.group();
        Strategy currentStrategy= getStrategy(currentFormatField);
        for(;;) {
            patternMatcher.region(patternMatcher.end(), patternMatcher.regionEnd());
            if(!patternMatcher.lookingAt()) {
                nextStrategy = null;
                break;
            }
            String nextFormatField= patternMatcher.group();
            nextStrategy = getStrategy(nextFormatField);
            if(currentStrategy.addRegex(this, regex)) {
                collector.add(currentStrategy);
            }
            currentFormatField= nextFormatField;
            currentStrategy= nextStrategy;
        }
        if(currentStrategy.addRegex(this, regex)) {
            collector.add(currentStrategy);
        }
        currentFormatField= null;
        strategies= collector.toArray(new Strategy[collector.size()]);
        parsePattern= Pattern.compile(regex.toString());
    }

    // Accessors
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#getPattern()
     */
    @Override
    public String getPattern() {
        return pattern;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#getTimeZone()
     */
    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#getLocale()
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    // Give access to generated pattern for test code
    Pattern getParsePattern() {
        return parsePattern;
    }

    // Basics
    //-----------------------------------------------------------------------
    /**
     * <p>Compare another object for equality with this object.</p>
     *
     * @param obj  the object to compare to
     * @return <code>true</code>if equal to this instance
     */
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof FastDateParser) ) {
            return false;
        }
        FastDateParser other = (FastDateParser) obj;
        return pattern.equals(other.pattern)
            && timeZone.equals(other.timeZone)
            && locale.equals(other.locale);
    }

    /**
     * <p>Return a hashcode compatible with equals.</p>
     *
     * @return a hashcode compatible with equals
     */
    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }

    /**
     * <p>Get a string version of this formatter.</p>
     *
     * @return a debugging string
     */
    @Override
    public String toString() {
        return "FastDateParser[" + pattern + "," + locale + "," + timeZone.getID() + "]";
    }

    // Serializing
    //-----------------------------------------------------------------------
    /**
     * Create the object after serialization. This implementation reinitializes the
     * transient properties.
     *
     * @param in ObjectInputStream from which the object is being deserialized.
     * @throws IOException if there is an IO issue.
     * @throws ClassNotFoundException if a class cannot be found.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#parseObject(java.lang.String)
     */
    @Override
    public Object parseObject(String source) throws ParseException {
        return parse(source);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#parse(java.lang.String)
     */
    @Override
    public Date parse(String source) throws ParseException {
        Date date= parse(source, new ParsePosition(0));
        if(date==null) {
            // Add a note re supported date range
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new ParseException(
                        "(The " +locale + " locale does not support dates before 1868 AD)\n" +
                                "Unparseable date: \""+source+"\" does not match "+parsePattern.pattern(), 0);
            }
            throw new ParseException("Unparseable date: \""+source+"\" does not match "+parsePattern.pattern(), 0);
        }
        return date;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.lang3.time.DateParser#parse(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Date parse(String source, ParsePosition pos) {
        int offset= pos.getIndex();
        Matcher matcher= parsePattern.matcher(source.substring(offset));
        if(!matcher.lookingAt()) {
            return null;
        }
        // timing tests indicate getting new instance is 19% faster than cloning
        Calendar cal= Calendar.getInstance(timeZone, locale);
        cal.clear();

        for(int i=0; i<strategies.length;) {
            Strategy strategy= strategies[i++];
            strategy.setCalendar(this, cal, matcher.group(i));
        }
        pos.setIndex(offset+matcher.end());
        return cal.getTime();
    }

    // Support for strategies
    //-----------------------------------------------------------------------

    /**
     * Escape constant fields into regular expression
     * @param regex The destination regex
     * @param value The source field
     * @param unquote If true, replace two success quotes ('') with single quote (')
     * @return The <code>StringBuilder</code>
     */
    private static StringBuilder escapeRegex(StringBuilder regex, String value, boolean unquote) {
        boolean wasWhite= false;
        for(int i= 0; i<value.length(); ++i) {
            char c= value.charAt(i);
            if(Character.isWhitespace(c)) {
                if(!wasWhite) {
                    wasWhite= true;
                    regex.append(c);
                }
                continue;
            }
            wasWhite= false;
            switch(c) {
            case '\'':
                if(unquote) {
                    if(++i==value.length()) {
                        return regex;
                    }
                    c= value.charAt(i);
                }
                break;
            case '?':
            case '[':
            case ']':
            case '(':
            case ')':
            case '{':
            case '}':
            case '\\':
            case '|':
            case '*':
            case '+':
            case '^':
            case '$':
            case '.':
                regex.append('\\');
            }
            regex.append(c);
        }
        return regex;
    }

    /**
     * A class to store Key / Value pairs
     */
    private static class KeyValue {
        public String key;
        public int value;

        /**
         * Construct a Key / Value pair
         * @param key The key
         * @param value The value
         */
        public KeyValue(String key, int value) {
            this.key= key;
            this.value= value;
        }
    }

    /**
     * ignore case comparison of keys
     */
    private static final Comparator<KeyValue> IGNORE_CASE_COMPARATOR = new Comparator<KeyValue> () {
        @Override
        public int compare(KeyValue left, KeyValue right) {
            return left.key.compareToIgnoreCase(right.key);
        }
    };

    /**
     * Get the short and long values displayed for a field
     * @param field The field of interest
     * @return A sorted array of the field key / value pairs
     */
    KeyValue[] getDisplayNames(int field) {
        Integer fieldInt = Integer.valueOf(field);
        KeyValue[] fieldKeyValues= nameValues.get(fieldInt);
        if(fieldKeyValues==null) {
            DateFormatSymbols symbols= DateFormatSymbols.getInstance(locale);
            switch(field) {
            case Calendar.ERA:
                // DateFormatSymbols#getEras() only returns AD/BC or translations
                // It does not work for the Thai Buddhist or Japanese Imperial calendars.
                // see: https://issues.apache.org/jira/browse/TRINIDAD-2126
                Calendar c = Calendar.getInstance(locale);
                // N.B. Some calendars have different short and long symbols, e.g. ja_JP_JP
                String[] shortEras = toArray(c.getDisplayNames(Calendar.ERA, Calendar.SHORT, locale));
                String[] longEras = toArray(c.getDisplayNames(Calendar.ERA, Calendar.LONG, locale));
                fieldKeyValues= createKeyValues(longEras, shortEras);
                break;
            case Calendar.DAY_OF_WEEK:
                fieldKeyValues= createKeyValues(symbols.getWeekdays(), symbols.getShortWeekdays());
                break;
            case Calendar.AM_PM:
                fieldKeyValues= createKeyValues(symbols.getAmPmStrings(), null);
                break;
            case Calendar.MONTH:
                fieldKeyValues= createKeyValues(symbols.getMonths(), symbols.getShortMonths());
                break;
            default:
                throw new IllegalArgumentException("Invalid field value "+field);
            }
            KeyValue[] prior = nameValues.putIfAbsent(fieldInt, fieldKeyValues);
            if(prior!=null) {
                fieldKeyValues= prior;
            }
        }
        return fieldKeyValues;
    }

    private String[] toArray(Map<String, Integer> era) {
        String[] eras = new String[era.size()]; // assume no gaps in entry values
        for(Map.Entry<String, Integer> me : era.entrySet()) {
            int idx = me.getValue().intValue();
            final String key = me.getKey();
            if (key == null) {
                throw new IllegalArgumentException();
            }
            eras[idx] = key;
        }
        return eras;
    }

    /**
     * Create key / value pairs from keys
     * @param longValues The allowable long names for a field
     * @param shortValues The optional allowable short names for a field
     * @return The sorted name / value pairs for the field
     */
    private static KeyValue[] createKeyValues(String[] longValues, String[] shortValues) {
        KeyValue[] fieldKeyValues= new KeyValue[count(longValues)+count(shortValues)];
        copy(fieldKeyValues, copy(fieldKeyValues, 0, longValues), shortValues);
        Arrays.sort(fieldKeyValues, IGNORE_CASE_COMPARATOR);
        return fieldKeyValues;
    }

    /**
     * Get a count of valid values in array.  A valid value is of non-zero length.
     * @param values The values to check.  This parameter may be null
     * @return The number of valid values
     */
    private static int count(String[] values) {
        int count= 0;
        if(values!=null) {
            for(String value : values) {
                if(value.length()>0) {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * Create key / value pairs from values
     * @param fieldKeyValues The destination array
     * @param offset The offset into the destination array
     * @param values The values to use to create key / value pairs.  This parameter may be null.
     * @return The offset into the destination array
     */
    private static int copy(KeyValue[] fieldKeyValues, int offset, String[] values) {
        if(values!=null) {
            for(int i= 0; i<values.length; ++i) {
                String value= values[i];
                if(value.length()>0) {
                    fieldKeyValues[offset++]= new KeyValue(value, i);
                }
            }
        }
        return offset;
    }

    /**
     * Adjust dates to be within 80 years before and 20 years after instantiation
     * @param twoDigitYear The year to adjust
     * @return A value within -80 and +20 years from instantiation of this instance
     */
    int adjustYear(int twoDigitYear) {
        int trial= twoDigitYear + thisYear - thisYear%100;
        if(trial < thisYear+20) {
            return trial;
        }
        return trial-100;
    }

    /**
     * Is the next field a number?
     * @return true, if next field will be a number
     */
    boolean isNextNumber() {
        return nextStrategy!=null && nextStrategy.isNumber();
    }

    /**
     * What is the width of the current field?
     * @return The number of characters in the current format field
     */
    int getFieldWidth() {
        return currentFormatField.length();
    }

    /**
     * A strategy to parse a single field from the parsing pattern
     */
    private interface Strategy {
        /**
         * Is this field a number?
         * @return true, if field is a number
         */
        boolean isNumber();
        /**
         * Set the Calendar with the parsed field
         * @param parser The parser calling this strategy
         * @param cal The <code>Calendar</code> to set
         * @param value The parsed field to translate and set in cal
         */
        void setCalendar(FastDateParser parser, Calendar cal, String value);
        /**
         * Generate a <code>Pattern</code> regular expression to the <code>StringBuilder</code>
         * which will accept this field
         * @param parser The parser calling this strategy
         * @param regex The <code>StringBuilder</code> to append to
         * @return true, if this field will set the calendar;
         * false, if this field is a constant value
         */
        boolean addRegex(FastDateParser parser, StringBuilder regex);
    }

    /**
     * A <code>Pattern</code> to parse the user supplied SimpleDateFormat pattern
     */
    private static final Pattern formatPattern= Pattern.compile(
            "D+|E+|F+|G+|H+|K+|M+|S+|W+|Z+|a+|d+|h+|k+|m+|s+|w+|y+|z+|''|'[^']++(''[^']*+)*+'|[^'A-Za-z]++");

    /**
     * Obtain a Strategy given a field from a SimpleDateFormat pattern
     * @param formatField A sub-sequence of the SimpleDateFormat pattern
     * @return The Strategy that will handle parsing for the field
     */
    private Strategy getStrategy(String formatField) {
        switch(formatField.charAt(0)) {
        case '\'':
            if(formatField.length()>2) {
                formatField= formatField.substring(1, formatField.length()-1);
            }
            //$FALL-THROUGH$
        default:
            return new CopyQuotedStrategy(formatField);
        case 'D':
            return DAY_OF_YEAR_STRATEGY;
        case 'E':
            return DAY_OF_WEEK_STRATEGY;
        case 'F':
            return DAY_OF_WEEK_IN_MONTH_STRATEGY;
        case 'G':
            return ERA_STRATEGY;
        case 'H':
            return MODULO_HOUR_OF_DAY_STRATEGY;
        case 'K':
            return HOUR_STRATEGY;
        case 'M':
            return formatField.length()>=3 ?TEXT_MONTH_STRATEGY :NUMBER_MONTH_STRATEGY;
        case 'S':
            return MILLISECOND_STRATEGY;
        case 'W':
            return WEEK_OF_MONTH_STRATEGY;
        case 'Z':
            break;
        case 'a':
            return AM_PM_STRATEGY;
        case 'd':
            return DAY_OF_MONTH_STRATEGY;
        case 'h':
            return MODULO_HOUR_STRATEGY;
        case 'k':
            return HOUR_OF_DAY_STRATEGY;
        case 'm':
            return MINUTE_STRATEGY;
        case 's':
            return SECOND_STRATEGY;
        case 'w':
            return WEEK_OF_YEAR_STRATEGY;
        case 'y':
            return formatField.length()>2 ?LITERAL_YEAR_STRATEGY :ABBREVIATED_YEAR_STRATEGY;
        case 'z':
            break;
        }
        TimeZoneStrategy tzs= tzsCache.get(locale);
        if(tzs==null) {
            tzs= new TimeZoneStrategy(locale);
            TimeZoneStrategy inCache= tzsCache.putIfAbsent(locale, tzs);
            if(inCache!=null) {
                return inCache;
            }
        }
        return tzs;
    }

    /**
     * A strategy that copies the static or quoted field in the parsing pattern
     */
    private static class CopyQuotedStrategy implements Strategy {
        private final String formatField;

        /**
         * Construct a Strategy that ensures the formatField has literal text
         * @param formatField The literal text to match
         */
        CopyQuotedStrategy(String formatField) {
            this.formatField= formatField;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNumber() {
            char c= formatField.charAt(0);
            if(c=='\'') {
                c= formatField.charAt(1);
            }
            return Character.isDigit(c);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            escapeRegex(regex, formatField, true);
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
        }
    }

    /**
     * A strategy that handles a text field in the parsing pattern
     */
    private static class TextStrategy implements Strategy {
        private final int field;

        /**
         * Construct a Strategy that parses a Text field
         * @param field The Calendar field
         */
        TextStrategy(int field) {
            this.field= field;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNumber() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            regex.append('(');
            for(KeyValue textKeyValue : parser.getDisplayNames(field)) {
                escapeRegex(regex, textKeyValue.key, false).append('|');
            }
            regex.setCharAt(regex.length()-1, ')');
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            KeyValue[] textKeyValues= parser.getDisplayNames(field);
            int idx= Arrays.binarySearch(textKeyValues, new KeyValue(value, -1), IGNORE_CASE_COMPARATOR);
            if(idx<0) {
                StringBuilder sb= new StringBuilder(value);
                sb.append(" not in (");
                for(KeyValue textKeyValue : textKeyValues) {
                    sb.append(textKeyValue.key).append(' ');
                }
                sb.setCharAt(sb.length()-1, ')');
                throw new IllegalArgumentException(sb.toString());
            }
            cal.set(field, textKeyValues[idx].value);
        }
    }

    /**
     * A strategy that handles a number field in the parsing pattern
     */
    private static class NumberStrategy implements Strategy {
        protected final int field;

        /**
         * Construct a Strategy that parses a Number field
         * @param field The Calendar field
         */
        NumberStrategy(int field) {
             this.field= field;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNumber() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            if(parser.isNextNumber()) {
                regex.append("(\\p{IsNd}{").append(parser.getFieldWidth()).append("}+)");
            }
            else {
                regex.append("(\\p{IsNd}++)");
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            cal.set(field, modify(Integer.parseInt(value)));
        }

        /**
         * Make any modifications to parsed integer
         * @param iValue The parsed integer
         * @return The modified value
         */
        public int modify(int iValue) {
            return iValue;
        }
    }

    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {
        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            int iValue= Integer.parseInt(value);
            if(iValue<100) {
                iValue= parser.adjustYear(iValue);
            }
            cal.set(Calendar.YEAR, iValue);
        }
    };

    /**
     * A strategy that handles a timezone field in the parsing pattern
     */
    private static class TimeZoneStrategy implements Strategy {

        final String validTimeZoneChars;
        final SortedMap<String, TimeZone> tzNames= new TreeMap<String, TimeZone>(String.CASE_INSENSITIVE_ORDER);

        /**
         * Construct a Strategy that parses a TimeZone
         * @param locale The Locale
         */
        TimeZoneStrategy(Locale locale) {
            for(String id : TimeZone.getAvailableIDs()) {
                if(id.startsWith("GMT")) {
                    continue;
                }
                TimeZone tz= TimeZone.getTimeZone(id);
                tzNames.put(tz.getDisplayName(false, TimeZone.SHORT, locale), tz);
                tzNames.put(tz.getDisplayName(false, TimeZone.LONG, locale), tz);
                if(tz.useDaylightTime()) {
                    tzNames.put(tz.getDisplayName(true, TimeZone.SHORT, locale), tz);
                    tzNames.put(tz.getDisplayName(true, TimeZone.LONG, locale), tz);
                }
            }
            StringBuilder sb= new StringBuilder();
            sb.append("(GMT[+\\-]\\d{0,1}\\d{2}|[+\\-]\\d{2}:?\\d{2}|");
            for(String id : tzNames.keySet()) {
                escapeRegex(sb, id, false).append('|');
            }
            sb.setCharAt(sb.length()-1, ')');
            validTimeZoneChars= sb.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNumber() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            regex.append(validTimeZoneChars);
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            TimeZone tz;
            if(value.charAt(0)=='+' || value.charAt(0)=='-') {
                tz= TimeZone.getTimeZone("GMT"+value);
            }
            else if(value.startsWith("GMT")) {
                tz= TimeZone.getTimeZone(value);
            }
            else {
                tz= tzNames.get(value);
                if(tz==null) {
                    throw new IllegalArgumentException(value + " is not a supported timezone name");
                }
            }
            cal.setTimeZone(tz);
        }
    }


    private static final Strategy ERA_STRATEGY = new TextStrategy(Calendar.ERA);
    private static final Strategy DAY_OF_WEEK_STRATEGY = new TextStrategy(Calendar.DAY_OF_WEEK);
    private static final Strategy AM_PM_STRATEGY = new TextStrategy(Calendar.AM_PM);
    private static final Strategy TEXT_MONTH_STRATEGY = new TextStrategy(Calendar.MONTH);

    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {
        @Override
        public int modify(int iValue) {
            return iValue-1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);
    private static final Strategy MODULO_HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {
        @Override
        public int modify(int iValue) {
            return iValue%24;
        }
    };
    private static final Strategy MODULO_HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR) {
        @Override
        public int modify(int iValue) {
            return iValue%12;
        }
    };
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);
}
