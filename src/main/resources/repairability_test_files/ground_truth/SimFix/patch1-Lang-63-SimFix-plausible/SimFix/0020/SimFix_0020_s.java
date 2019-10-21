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
package org.apache.commons.lang.time;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>Duration formatting utilities and constants. The following table describes the tokens 
 * used in the pattern language for formatting. </p>
 * <table border="1">
 *  <tr><th>character</th><th>duration element</th></tr>
 *  <tr><td>y</td><td>years</td></tr>
 *  <tr><td>M</td><td>months</td></tr>
 *  <tr><td>d</td><td>days</td></tr>
 *  <tr><td>H</td><td>hours</td></tr>
 *  <tr><td>m</td><td>minutes</td></tr>
 *  <tr><td>s</td><td>seconds</td></tr>
 *  <tr><td>S</td><td>milliseconds</td></tr>
 * </table>
 *
 * @author Apache Ant - DateUtils
 * @author <a href="mailto:sbailliez@apache.org">Stephane Bailliez</a>
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @author Stephen Colebourne
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @author Henri Yandell
 * @since 2.1
 * @version $Id$
 */
public class DurationFormatUtils {

    /**
     * <p>DurationFormatUtils instances should NOT be constructed in standard programming.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public DurationFormatUtils() {
        super();
    }

    /**
     * <p>Pattern used with <code>FastDateFormat</code> and <code>SimpleDateFormat</code>
     * for the ISO8601 period format used in durations.</p>
     * 
     * @see org.apache.commons.lang.time.FastDateFormat
     * @see java.text.SimpleDateFormat
     */
    public static final String ISO_EXTENDED_FORMAT_PATTERN = "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'";

    //-----------------------------------------------------------------------
    /**
     * <p>Formats the time gap as a string.</p>
     * 
     * <p>The format used is ISO8601-like:
     * <i>H</i>:<i>m</i>:<i>s</i>.<i>S</i>.</p>
     * 
     * @param durationMillis  the duration to format
     * @return the time as a String
     */
    public static String formatDurationHMS(long durationMillis) {
        return formatDuration(durationMillis, "H:mm:ss.SSS");
    }

    /**
     * <p>Formats the time gap as a string.</p>
     * 
     * <p>The format used is the ISO8601 period format.</p>
     * 
     * <p>This method formats durations using the days and lower fields of the
     * ISO format pattern, such as P7D6TH5M4.321S.</p>
     * 
     * @param durationMillis  the duration to format
     * @return the time as a String
     */
    public static String formatDurationISO(long durationMillis) {
        return formatDuration(durationMillis, ISO_EXTENDED_FORMAT_PATTERN, false);
    }

    /**
     * <p>Formats the time gap as a string, using the specified format, and padding with zeros and 
     * using the default timezone.</p>
     * 
     * <p>This method formats durations using the days and lower fields of the
     * format pattern. Months and larger are not used.</p>
     * 
     * @param durationMillis  the duration to format
     * @param format  the way in which to format the duration
     * @return the time as a String
     */
    public static String formatDuration(long durationMillis, String format) {
        return formatDuration(durationMillis, format, true);
    }

    /**
     * <p>Formats the time gap as a string, using the specified format.
     * Padding the left hand side of numbers with zeroes is optional and 
     * the timezone may be specified.</p>
     * 
     * <p>This method formats durations using the days and lower fields of the
     * format pattern. Months and larger are not used.</p>
     * 
     * @param durationMillis  the duration to format
     * @param format  the way in which to format the duration
     * @param padWithZeros  whether to pad the left hand side of numbers with 0's
     * @return the time as a String
     */
    public static String formatDuration(long durationMillis, String format, boolean padWithZeros) {

        Token[] tokens = lexx(format);

        int days         = 0;
        int hours        = 0;
        int minutes      = 0;
        int seconds      = 0;
        int milliseconds = 0;
        
        if (Token.containsTokenWithValue(tokens, d) ) {
            days = (int) (durationMillis / DateUtils.MILLIS_PER_DAY);
            durationMillis = durationMillis - (days * DateUtils.MILLIS_PER_DAY);
        }
        if (Token.containsTokenWithValue(tokens, H) ) {
            hours = (int) (durationMillis / DateUtils.MILLIS_PER_HOUR);
            durationMillis = durationMillis - (hours * DateUtils.MILLIS_PER_HOUR);
        }
        if (Token.containsTokenWithValue(tokens, m) ) {
            minutes = (int) (durationMillis / DateUtils.MILLIS_PER_MINUTE);
            durationMillis = durationMillis - (minutes * DateUtils.MILLIS_PER_MINUTE);
        }
        if (Token.containsTokenWithValue(tokens, s) ) {
            seconds = (int) (durationMillis / DateUtils.MILLIS_PER_SECOND);
            durationMillis = durationMillis - (seconds * DateUtils.MILLIS_PER_SECOND);
        }
        if (Token.containsTokenWithValue(tokens, S) ) {
            milliseconds = (int) durationMillis;
        }

        return format(tokens, 0, 0, days, hours, minutes, seconds, milliseconds, padWithZeros);
    }

    /**
     * <p>Formats an elapsed time into a plurialization correct string.</p>
     * 
     * <p>This method formats durations using the days and lower fields of the
     * format pattern. Months and larger are not used.</p>
     * 
     * @param durationMillis  the elapsed time to report in milliseconds
     * @param suppressLeadingZeroElements  suppresses leading 0 elements
     * @param suppressTrailingZeroElements  suppresses trailing 0 elements
     * @return the formatted text in days/hours/minutes/seconds
     */
    public static String formatDurationWords(
        long durationMillis,
        boolean suppressLeadingZeroElements,
        boolean suppressTrailingZeroElements) {

        // This method is generally replacable by the format method, but 
        // there are a series of tweaks and special cases that require 
        // trickery to replicate.
        String duration = formatDuration(durationMillis, "d' days 'H' hours 'm' minutes 's' seconds'");
        if (suppressLeadingZeroElements) {
            // this is a temporary marker on the front. Like ^ in regexp.
            duration = " " + duration;
            String tmp = StringUtils.replaceOnce(duration, " 0 days", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
                if (tmp.length() != duration.length()) {
                    duration = tmp;
                    tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
                    duration = tmp;
                    if (tmp.length() != duration.length()) {
                        duration = StringUtils.replaceOnce(tmp, " 0 seconds", "");
                    }
                }
            }
            if (duration.length() != 0) {
                // strip the space off again
                duration = duration.substring(1);
            }
        }
        if (suppressTrailingZeroElements) {
            String tmp = StringUtils.replaceOnce(duration, " 0 seconds", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
                if (tmp.length() != duration.length()) {
                    duration = tmp;
                    tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
                    if (tmp.length() != duration.length()) {
                        duration = StringUtils.replaceOnce(tmp, " 0 days", "");
                    }
                }
            }
        }
        // handle plurals
        duration = " " + duration;
        duration = StringUtils.replaceOnce(duration, " 1 seconds", " 1 second");
        duration = StringUtils.replaceOnce(duration, " 1 minutes", " 1 minute");
        duration = StringUtils.replaceOnce(duration, " 1 hours", " 1 hour");
        duration = StringUtils.replaceOnce(duration, " 1 days", " 1 day");
        return duration.trim();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Formats the time gap as a string.</p>
     * 
     * <p>The format used is the ISO8601 period format.</p>
     * 
     * @param startMillis  the start of the duration to format
     * @param endMillis  the end of the duration to format
     * @return the time as a String
     */
    public static String formatPeriodISO(long startMillis, long endMillis) {
        return formatPeriod(startMillis, endMillis, ISO_EXTENDED_FORMAT_PATTERN, false, TimeZone.getDefault());
    }

    /**
     * <p>Formats the time gap as a string, using the specified format.
     * Padding the left hand side of numbers with zeroes is optional.
     * 
     * @param startMillis  the start of the duration
     * @param endMillis  the end of the duration
     * @param format  the way in which to format the duration
     * @return the time as a String
     */
    public static String formatPeriod(long startMillis, long endMillis, String format) {
        return formatPeriod(startMillis, endMillis, format, true, TimeZone.getDefault());
    }

    /**
     * <p>Formats the time gap as a string, using the specified format.
     * Padding the left hand side of numbers with zeroes is optional and 
     * the timezone may be specified. 
     * 
     * @param startMillis  the start of the duration
     * @param endMillis  the end of the duration
     * @param format  the way in which to format the duration
     * @param padWithZeros whether to pad the left hand side of numbers with 0's
     * @param timezone the millis are defined in
     * @return the time as a String
     */
    public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, 
            TimeZone timezone) {

        long millis = endMillis - startMillis;
        if (millis < 28 * DateUtils.MILLIS_PER_DAY) {
            return formatDuration(millis, format, padWithZeros);
        }

        Token[] tokens = lexx(format);

        // timezones get funky around 0, so normalizing everything to GMT 
        // stops the hours being off
        Calendar start = Calendar.getInstance(timezone);
        start.setTime(new Date(startMillis));
        Calendar end = Calendar.getInstance(timezone);
        end.setTime(new Date(endMillis));

        // initial estimates
        int milliseconds = end.get(Calendar.MILLISECOND) - start.get(Calendar.MILLISECOND);
        int seconds = end.get(Calendar.SECOND) - start.get(Calendar.SECOND);
        int minutes = end.get(Calendar.MINUTE) - start.get(Calendar.MINUTE);
        int hours = end.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY);
        int days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);
        int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);

        // each initial estimate is adjusted in case it is under 0
        while (milliseconds < 0) {
            milliseconds += 1000;
            seconds -= 1;
        }
        while (seconds < 0) {
            seconds += 60;
            minutes -= 1;
        }
        while (minutes < 0) {
            minutes += 60;
            hours -= 1;
        }
        while (hours < 0) {
            hours += 24;
            days -= 1;
        }
        while (days < 0) {
            days += 31;
//days += 31; // TODO: Need tests to show this is bad and the new code is good.
// HEN: It's a tricky subject. Jan 15th to March 10th. If I count days-first it is 
// 1 month and 26 days, but if I count month-first then it is 1 month and 23 days.
// Also it's contextual - if asked for no M in the format then I should probably 
// be doing no calculating here.
            months -= 1;
        }
        while (months < 0) {
            months += 12;
            years -= 1;
        }
        milliseconds -= reduceAndCorrect(start, end, Calendar.MILLISECOND, milliseconds);
        seconds -= reduceAndCorrect(start, end, Calendar.SECOND, seconds);
        minutes -= reduceAndCorrect(start, end, Calendar.MINUTE, minutes);
        hours -= reduceAndCorrect(start, end, Calendar.HOUR_OF_DAY, hours);
        days -= reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, days);
        months -= reduceAndCorrect(start, end, Calendar.MONTH, months);
        years -= reduceAndCorrect(start, end, Calendar.YEAR, years);

        // This next block of code adds in values that 
        // aren't requested. This allows the user to ask for the 
        // number of months and get the real count and not just 0->11.
        if (!Token.containsTokenWithValue(tokens, y)) {
            if (Token.containsTokenWithValue(tokens, M)) {
                months += 12 * years;
                years = 0;
            } else {
                // TODO: this is a bit weak, needs work to know about leap years
                days += 365 * years;
                years = 0;
            }
        }
        if (!Token.containsTokenWithValue(tokens, M)) {
            days += end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
            months = 0;
        }
        if (!Token.containsTokenWithValue(tokens, d)) {
            hours += 24 * days;
            days = 0;
        }
        if (!Token.containsTokenWithValue(tokens, H)) {
            minutes += 60 * hours;
            hours = 0;
        }
        if (!Token.containsTokenWithValue(tokens, m)) {
            seconds += 60 * minutes;
            minutes = 0;
        }
        if (!Token.containsTokenWithValue(tokens, s)) {
            milliseconds += 1000 * seconds;
            seconds = 0;
        }

        return format(tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>The internal method to do the formatting.</p>
     * 
     * @param tokens  the tokens
     * @param years  the number of years
     * @param months  the number of months
     * @param days  the number of days
     * @param hours  the number of hours
     * @param minutes  the number of minutes
     * @param seconds  the number of seconds
     * @param milliseconds  the number of millis
     * @param padWithZeros  whether to pad
     * @return the formetted string
     */
    static String format(Token[] tokens, int years, int months, int days, int hours, int minutes, int seconds,
            int milliseconds, boolean padWithZeros) {
        StringBuffer buffer = new StringBuffer();
        boolean lastOutputSeconds = false;
        int sz = tokens.length;
        for (int i = 0; i < sz; i++) {
            Token token = tokens[i];
            Object value = token.getValue();
            int count = token.getCount();
            if (value instanceof StringBuffer) {
                buffer.append(value.toString());
            } else {
                if (value == y) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(years), count, '0') : Integer
                            .toString(years));
                    lastOutputSeconds = false;
                } else if (value == M) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(months), count, '0') : Integer
                            .toString(months));
                    lastOutputSeconds = false;
                } else if (value == d) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(days), count, '0') : Integer
                            .toString(days));
                    lastOutputSeconds = false;
                } else if (value == H) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(hours), count, '0') : Integer
                            .toString(hours));
                    lastOutputSeconds = false;
                } else if (value == m) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(minutes), count, '0') : Integer
                            .toString(minutes));
                    lastOutputSeconds = false;
                } else if (value == s) {
                    buffer.append(padWithZeros ? StringUtils.leftPad(Integer.toString(seconds), count, '0') : Integer
                            .toString(seconds));
                    lastOutputSeconds = true;
                } else if (value == S) {
                    if (lastOutputSeconds) {
                        milliseconds += 1000;
                        String str = padWithZeros
                                ? StringUtils.leftPad(Integer.toString(milliseconds), count, '0')
                                : Integer.toString(milliseconds);
                        buffer.append(str.substring(1));
                    } else {
                        buffer.append(padWithZeros
                                ? StringUtils.leftPad(Integer.toString(milliseconds), count, '0')
                                : Integer.toString(milliseconds));
                    }
                    lastOutputSeconds = false;
                }
            }
        }
        return buffer.toString();
    }
    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            end.add( field, newdiff );
            return newdiff;
        } else {
            return 0;
        }
    }

    static final Object y = "y";
    static final Object M = "M";
    static final Object d = "d";
    static final Object H = "H";
    static final Object m = "m";
    static final Object s = "s";
    static final Object S = "S";
    
    /**
     * Parses a classic date format string into Tokens
     *
     * @param format to parse
     * @return Token[] of tokens
     */
    static Token[] lexx(String format) {
        char[] array = format.toCharArray();
        java.util.ArrayList list = new java.util.ArrayList(array.length);

        boolean inLiteral = false;
        StringBuffer buffer = null;
        Token previous = null;
        int sz = array.length;
        for(int i=0; i<sz; i++) {
            char ch = array[i];
            if(inLiteral && ch != '\'') {
                buffer.append(ch);
                continue;
            }
            Object value = null;
            switch(ch) {
                // TODO: Need to handle escaping of '
                case '\'' : 
                  if(inLiteral) {
                      buffer = null;
                      inLiteral = false;
                  } else {
                      buffer = new StringBuffer();
                      list.add(new Token(buffer));
                      inLiteral = true;
                  }
                  break;
                case 'y'  : value = y; break;
                case 'M'  : value = M; break;
                case 'd'  : value = d; break;
                case 'H'  : value = H; break;
                case 'm'  : value = m; break;
                case 's'  : value = s; break;
                case 'S'  : value = S; break;
                default   : 
                  if(buffer == null) {
                      buffer = new StringBuffer();
                      list.add(new Token(buffer));
                  }
                  buffer.append(ch);
            }

            if(value != null) {
                if(previous != null && previous.getValue() == value) {
                    previous.increment();
                } else {
                    Token token = new Token(value);
                    list.add(token); 
                    previous = token;
                }
                buffer = null; 
            }
        }
        return (Token[]) list.toArray( new Token[0] );
    }

    /**
     * Element that is parsed from the format pattern.
     */
    static class Token {

        /**
         * Helper method to determine if a set of tokens contain a value
         *
         * @param tokens set to look in
         * @param value to look for
         * @return boolean <code>true</code> if contained
         */
        static boolean containsTokenWithValue(Token[] tokens, Object value) {
            int sz = tokens.length;
            for (int i = 0; i < sz; i++) {
                if (tokens[i].getValue() == value) {
                    return true;
                }
            }
            return false;
        }

        private Object value;
        private int count;

        /**
         * Wraps a token around a value. A value would be something like a 'Y'.
         *
         * @param value to wrap
         */
        Token(Object value) {
            this.value = value;
            this.count = 1;
        }

        /**
         * Wraps a token around a repeated number of a value, for example it would 
         * store 'yyyy' as a value for y and a count of 4.
         *
         * @param value to wrap
         * @param count to wrap
         */
        Token(Object value, int count) {
            this.value = value;
            this.count = count;
        }

        /**
         * Adds another one of the value
         */
        void increment() { 
            count++;
        }

        /**
         * Gets the current number of values represented
         *
         * @return int number of values represented
         */
        int getCount() {
            return count;
        }

        /**
         * Gets the particular value this token represents.
         * 
         * @return Object value
         */
        Object getValue() {
            return value;
        }

        /**
         * Supports equality of this Token to another Token.
         *
         * @param obj2 Object to consider equality of
         * @return boolean <code>true</code> if equal
         */
        public boolean equals(Object obj2) {
            if (obj2 instanceof Token) {
                Token tok2 = (Token) obj2;
                if (this.value.getClass() != tok2.value.getClass()) {
                    return false;
                }
                if (this.count != tok2.count) {
                    return false;
                }
                if (this.value instanceof StringBuffer) {
                    return this.value.toString().equals(tok2.value.toString());
                } else if (this.value instanceof Number) {
                    return this.value.equals(tok2.value);
                } else {
                    return this.value == tok2.value;
                }
            } else {
                return false;
            }
        }

        /**
         * Returns a hashcode for the token equal to the 
         * hashcode for the token's value. Thus 'TT' and 'TTTT' 
         * will have the same hashcode. 
         *
         * @return The hashcode for the token
         */
        public int hashCode() {
            return this.value.hashCode();
        }

        /**
         * Represents this token as a String.
         *
         * @return String representation of the token
         */
        public String toString() {
            return StringUtils.repeat(this.value.toString(), this.count);
        }
    }

}
