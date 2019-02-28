/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ---------------------
 * DatasetUtilities.java
 * ---------------------
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Andrzej Porebski (bug fix);
 *                   Jonathan Nash (bug fix);
 *                   Richard Atkinson;
 *                   Andreas Schroeder;
 *
 * $Id: DatasetUtilities.java,v 1.18.2.8 2007/06/12 14:54:30 mungady Exp $
 *
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 15-Nov-2001 : Moved to package com.jrefinery.data.* in the JCommon class 
 *               library (DG);
 *               Changed to handle null values from datasets (DG);
 *               Bug fix (thanks to Andrzej Porebski) - initial value now set 
 *               to positive or negative infinity when iterating (DG);
 * 22-Nov-2001 : Datasets with containing no data now return null for min and 
 *               max calculations (DG);
 * 13-Dec-2001 : Extended to handle HighLowDataset and IntervalXYDataset (DG);
 * 15-Feb-2002 : Added getMinimumStackedRangeValue() and 
 *               getMaximumStackedRangeValue() (DG);
 * 28-Feb-2002 : Renamed Datasets.java --> DatasetUtilities.java (DG);
 * 18-Mar-2002 : Fixed bug in min/max domain calculation for datasets that 
 *               implement the CategoryDataset interface AND the XYDataset 
 *               interface at the same time.  Thanks to Jonathan Nash for the 
 *               fix (DG);
 * 23-Apr-2002 : Added getDomainExtent() and getRangeExtent() methods (DG);
 * 13-Jun-2002 : Modified range measurements to handle 
 *               IntervalCategoryDataset (DG);
 * 12-Jul-2002 : Method name change in DomainInfo interface (DG);
 * 30-Jul-2002 : Added pie dataset summation method (DG);
 * 01-Oct-2002 : Added a method for constructing an XYDataset from a Function2D
 *               instance (DG);
 * 24-Oct-2002 : Amendments required following changes to the CategoryDataset 
 *               interface (DG);
 * 18-Nov-2002 : Changed CategoryDataset to TableDataset (DG);
 * 04-Mar-2003 : Added isEmpty(XYDataset) method (DG);
 * 05-Mar-2003 : Added a method for creating a CategoryDataset from a 
 *               KeyedValues instance (DG);
 * 15-May-2003 : Renamed isEmpty --> isEmptyOrNull (DG);
 * 25-Jun-2003 : Added limitPieDataset methods (RA);
 * 26-Jun-2003 : Modified getDomainExtent() method to accept null datasets (DG);
 * 27-Jul-2003 : Added getStackedRangeExtent(TableXYDataset data) (RA);
 * 18-Aug-2003 : getStackedRangeExtent(TableXYDataset data) now handles null 
 *               values (RA);
 * 02-Sep-2003 : Added method to check for null or empty PieDataset (DG);
 * 18-Sep-2003 : Fix for bug 803660 (getMaximumRangeValue for 
 *               CategoryDataset) (DG);
 * 20-Oct-2003 : Added getCumulativeRangeExtent() method (DG);
 * 09-Jan-2003 : Added argument checking code to the createCategoryDataset() 
 *               method (DG);
 * 23-Mar-2004 : Fixed bug in getMaximumStackedRangeValue() method (DG);
 * 31-Mar-2004 : Exposed the extent iteration algorithms to use one of them and 
 *               applied noninstantiation pattern (AS);
 * 11-May-2004 : Renamed getPieDatasetTotal --> calculatePieDatasetTotal (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with getYValue();
 * 24-Aug-2004 : Added argument checks to createCategoryDataset() method (DG);
 * 04-Oct-2004 : Renamed ArrayUtils --> ArrayUtilities (DG);
 * 06-Oct-2004 : Renamed findDomainExtent() --> findDomainBounds(),
 *               findRangeExtent() --> findRangeBounds() (DG);
 * 07-Jan-2005 : Renamed findStackedRangeExtent() --> findStackedRangeBounds(),
 *               findCumulativeRangeExtent() --> findCumulativeRangeBounds(),
 *               iterateXYRangeExtent() --> iterateXYRangeBounds(), 
 *               removed deprecated methods (DG);
 * 03-Feb-2005 : The findStackedRangeBounds() methods now return null for 
 *               empty datasets (DG);
 * 03-Mar-2005 : Moved createNumberArray() and createNumberArray2D() methods
 *               from DatasetUtilities --> DataUtilities (DG);
 * 22-Sep-2005 : Added new findStackedRangeBounds() method that takes base
 *               argument (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 15-Mar-2007 : Added calculateStackTotal() method (DG);
 * 21-Jun-2007 : Removed JCommon dependencies (DG);
 * 
 */

package org.jfree.data.general;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.util.ArrayUtilities;
import org.jfree.data.DomainInfo;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.KeyedValues;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A collection of useful static methods relating to datasets.
 */
public final class DatasetUtilities {
    
    /**
     * Private constructor for non-instanceability.
     */
    private DatasetUtilities() {
        // now try to instantiate this ;-)
    }

    /**
     * Calculates the total of all the values in a {@link PieDataset}.  If 
     * the dataset contains negative or <code>null</code> values, they are 
     * ignored. 
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The total.
     */
    public static double calculatePieDatasetTotal(PieDataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        List keys = dataset.getKeys();
        double totalValue = 0;
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Comparable current = (Comparable) iterator.next();
            if (current != null) {
                Number value = dataset.getValue(current);
                double v = 0.0;
                if (value != null) {
                    v = value.doubleValue();
                }
                if (v > 0) {
                    totalValue = totalValue + v;
                }
            }
        }
        return totalValue;
    }

    /**
     * Creates a pie dataset from a table dataset by taking all the values
     * for a single row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param rowKey  the row key.
     *
     * @return A pie dataset.
     */
    public static PieDataset createPieDatasetForRow(CategoryDataset dataset, 
                                                    Comparable rowKey) {
        int row = dataset.getRowIndex(rowKey);
        return createPieDatasetForRow(dataset, row);
    }

    /**
     * Creates a pie dataset from a table dataset by taking all the values
     * for a single row.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param row  the row (zero-based index).
     *
     * @return A pie dataset.
     */
    public static PieDataset createPieDatasetForRow(CategoryDataset dataset, 
                                                    int row) {
        DefaultPieDataset result = new DefaultPieDataset();
        int columnCount = dataset.getColumnCount();
        for (int current = 0; current < columnCount; current++) {
            Comparable columnKey = dataset.getColumnKey(current);
            result.setValue(columnKey, dataset.getValue(row, current));
        }
        return result;
    }

    /**
     * Creates a pie dataset from a table dataset by taking all the values
     * for a single column.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param columnKey  the column key.
     *
     * @return A pie dataset.
     */
    public static PieDataset createPieDatasetForColumn(CategoryDataset dataset,
                                                       Comparable columnKey) {
        int column = dataset.getColumnIndex(columnKey);
        return createPieDatasetForColumn(dataset, column);
    }

    /**
     * Creates a pie dataset from a {@link CategoryDataset} by taking all the 
     * values for a single column.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param column  the column (zero-based index).
     *
     * @return A pie dataset.
     */
    public static PieDataset createPieDatasetForColumn(CategoryDataset dataset, 
                                                       int column) {
        DefaultPieDataset result = new DefaultPieDataset();
        int rowCount = dataset.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Comparable rowKey = dataset.getRowKey(i);
            result.setValue(rowKey, dataset.getValue(i, column));
        }
        return result;
    }

    /**
     * Creates a new pie dataset based on the supplied dataset, but modified
     * by aggregating all the low value items (those whose value is lower
     * than the <code>percentThreshold</code>) into a single item with the
     * key "Other".
     *
     * @param source  the source dataset (<code>null</code> not permitted).
     * @param key  a new key for the aggregated items (<code>null</code> not
     *             permitted).
     * @param minimumPercent  the percent threshold.
     * 
     * @return The pie dataset with (possibly) aggregated items.
     */
    public static PieDataset createConsolidatedPieDataset(PieDataset source, 
                                                          Comparable key,
                                                          double minimumPercent)
    {
        return DatasetUtilities.createConsolidatedPieDataset(
            source, key, minimumPercent, 2
        );
    }

    /**
     * Creates a new pie dataset based on the supplied dataset, but modified 
     * by aggregating all the low value items (those whose value is lower 
     * than the <code>percentThreshold</code>) into a single item.  The 
     * aggregated items are assigned the specified key.  Aggregation only 
     * occurs if there are at least <code>minItems</code> items to aggregate.
     *
     * @param source  the source dataset (<code>null</code> not permitted).
     * @param key  the key to represent the aggregated items.
     * @param minimumPercent  the percent threshold (ten percent is 0.10).
     * @param minItems  only aggregate low values if there are at least this 
     *                  many.
     * 
     * @return The pie dataset with (possibly) aggregated items.
     */
    public static PieDataset createConsolidatedPieDataset(PieDataset source,
                                                          Comparable key,
                                                          double minimumPercent,
                                                          int minItems) {
        
        DefaultPieDataset result = new DefaultPieDataset();
        double total = DatasetUtilities.calculatePieDatasetTotal(source);

        //  Iterate and find all keys below threshold percentThreshold
        List keys = source.getKeys();
        ArrayList otherKeys = new ArrayList();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Comparable currentKey = (Comparable) iterator.next();
            Number dataValue = source.getValue(currentKey);
            if (dataValue != null) {
                double value = dataValue.doubleValue();
                if (value / total < minimumPercent) {
                    otherKeys.add(currentKey);
                }
            }
        }

        //  Create new dataset with keys above threshold percentThreshold
        iterator = keys.iterator();
        double otherValue = 0;
        while (iterator.hasNext()) {
            Comparable currentKey = (Comparable) iterator.next();
            Number dataValue = source.getValue(currentKey);
            if (dataValue != null) {
                if (otherKeys.contains(currentKey) 
                    && otherKeys.size() >= minItems) {
                    //  Do not add key to dataset
                    otherValue += dataValue.doubleValue();
                }
                else {
                    //  Add key to dataset
                    result.setValue(currentKey, dataValue);
                }
            }
        }
        //  Add other category if applicable
        if (otherKeys.size() >= minItems) {
            result.setValue(key, otherValue);
        }
        return result;
    }

    /**
     * Creates a {@link CategoryDataset} that contains a copy of the data in an
     * array (instances of <code>Double</code> are created to represent the 
     * data items).
     * <p>
     * Row and column keys are created by appending 0, 1, 2, ... to the 
     * supplied prefixes.
     *
     * @param rowKeyPrefix  the row key prefix.
     * @param columnKeyPrefix  the column key prefix.
     * @param data  the data.
     *
     * @return The dataset.
     */
    public static CategoryDataset createCategoryDataset(String rowKeyPrefix,
                                                        String columnKeyPrefix,
                                                        double[][] data) {

        DefaultCategoryDataset result = new DefaultCategoryDataset();
        for (int r = 0; r < data.length; r++) {
            String rowKey = rowKeyPrefix + (r + 1);
            for (int c = 0; c < data[r].length; c++) {
                String columnKey = columnKeyPrefix + (c + 1);
                result.addValue(new Double(data[r][c]), rowKey, columnKey);
            }
        }
        return result;

    }

    /**
     * Creates a {@link CategoryDataset} that contains a copy of the data in 
     * an array.
     * <p>
     * Row and column keys are created by appending 0, 1, 2, ... to the 
     * supplied prefixes.
     *
     * @param rowKeyPrefix  the row key prefix.
     * @param columnKeyPrefix  the column key prefix.
     * @param data  the data.
     *
     * @return The dataset.
     */
    public static CategoryDataset createCategoryDataset(String rowKeyPrefix,
                                                        String columnKeyPrefix,
                                                        Number[][] data) {

        DefaultCategoryDataset result = new DefaultCategoryDataset();
        for (int r = 0; r < data.length; r++) {
            String rowKey = rowKeyPrefix + (r + 1);
            for (int c = 0; c < data[r].length; c++) {
                String columnKey = columnKeyPrefix + (c + 1);
                result.addValue(data[r][c], rowKey, columnKey);
            }
        }
        return result;

    }

    /**
     * Creates a {@link CategoryDataset} that contains a copy of the data in 
     * an array (instances of <code>Double</code> are created to represent the 
     * data items).
     * <p>
     * Row and column keys are taken from the supplied arrays.
     *
     * @param rowKeys  the row keys (<code>null</code> not permitted).
     * @param columnKeys  the column keys (<code>null</code> not permitted).
     * @param data  the data.
     *
     * @return The dataset.
     */
    public static CategoryDataset createCategoryDataset(Comparable[] rowKeys,
                                                        Comparable[] columnKeys,
                                                        double[][] data) {

        // check arguments...
        if (rowKeys == null) {
            throw new IllegalArgumentException("Null 'rowKeys' argument.");
        }
        if (columnKeys == null) {
            throw new IllegalArgumentException("Null 'columnKeys' argument.");
        }
        if (ArrayUtilities.hasDuplicateItems(rowKeys)) {
            throw new IllegalArgumentException("Duplicate items in 'rowKeys'.");
        }
        if (ArrayUtilities.hasDuplicateItems(columnKeys)) {
            throw new IllegalArgumentException(
                "Duplicate items in 'columnKeys'."
            );
        }
        if (rowKeys.length != data.length) {
            throw new IllegalArgumentException(
                "The number of row keys does not match the number of rows in "
                + "the data array."
            );
        }
        int columnCount = 0;
        for (int r = 0; r < data.length; r++) {
            columnCount = Math.max(columnCount, data[r].length);
        }
        if (columnKeys.length != columnCount) {
            throw new IllegalArgumentException(
                "The number of column keys does not match the number of "
                + "columns in the data array."
            );
        }
        
        // now do the work...
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        for (int r = 0; r < data.length; r++) {
            Comparable rowKey = rowKeys[r];
            for (int c = 0; c < data[r].length; c++) {
                Comparable columnKey = columnKeys[c];
                result.addValue(new Double(data[r][c]), rowKey, columnKey);
            }
        }
        return result;

    }

    /**
     * Creates a {@link CategoryDataset} by copying the data from the supplied 
     * {@link KeyedValues} instance.
     *
     * @param rowKey  the row key (<code>null</code> not permitted).
     * @param rowData  the row data (<code>null</code> not permitted).
     *
     * @return A dataset.
     */
    public static CategoryDataset createCategoryDataset(Comparable rowKey, 
                                                        KeyedValues rowData) {

        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (rowData == null) {
            throw new IllegalArgumentException("Null 'rowData' argument.");
        }
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        for (int i = 0; i < rowData.getItemCount(); i++) {
            result.addValue(rowData.getValue(i), rowKey, rowData.getKey(i));
        }
        return result;

    }

    /**
     * Creates an {@link XYDataset} by sampling the specified function over a 
     * fixed range.
     *
     * @param f  the function (<code>null</code> not permitted).
     * @param start  the start value for the range.
     * @param end  the end value for the range.
     * @param samples  the number of sample points (must be > 1).
     * @param seriesKey  the key to give the resulting series 
     *                   (<code>null</code> not permitted).
     *
     * @return A dataset.
     */
    public static XYDataset sampleFunction2D(Function2D f, 
                                             double start, 
                                             double end, 
                                             int samples,
                                             Comparable seriesKey) {

        if (f == null) {
            throw new IllegalArgumentException("Null 'f' argument.");   
        }
        if (seriesKey == null) {
            throw new IllegalArgumentException("Null 'seriesKey' argument.");
        }
        if (start >= end) {
            throw new IllegalArgumentException("Requires 'start' < 'end'.");
        }
        if (samples < 2) {
            throw new IllegalArgumentException("Requires 'samples' > 1");
        }

        XYSeries series = new XYSeries(seriesKey);
        double step = (end - start) / samples;
        for (int i = 0; i <= samples; i++) {
            double x = start + (step * i);
            series.add(x, f.getValue(x));
        }
        XYSeriesCollection collection = new XYSeriesCollection(series);
        return collection;

    }

    /**
     * Returns <code>true</code> if the dataset is empty (or <code>null</code>),
     * and <code>false</code> otherwise.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean isEmptyOrNull(PieDataset dataset) {

        if (dataset == null) {
            return true;
        }

        int itemCount = dataset.getItemCount();
        if (itemCount == 0) {
            return true;
        }

        for (int item = 0; item < itemCount; item++) {
            Number y = dataset.getValue(item);
            if (y != null) {
                double yy = y.doubleValue();
                if (yy > 0.0) {
                    return false;
                }
            }
        }

        return true;

    }

    /**
     * Returns <code>true</code> if the dataset is empty (or <code>null</code>),
     * and <code>false</code> otherwise.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean isEmptyOrNull(CategoryDataset dataset) {

        if (dataset == null) {
            return true;
        }

        int rowCount = dataset.getRowCount();
        int columnCount = dataset.getColumnCount();
        if (rowCount == 0 || columnCount == 0) {
            return true;
        }

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (dataset.getValue(r, c) != null) {
                    return false;
                }

            }
        }

        return true;

    }

    /**
     * Returns <code>true</code> if the dataset is empty (or <code>null</code>),
     * and <code>false</code> otherwise.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public static boolean isEmptyOrNull(XYDataset dataset) {

        boolean result = true;

        if (dataset != null) {
            for (int s = 0; s < dataset.getSeriesCount(); s++) {
                if (dataset.getItemCount(s) > 0) {
                    result = false;
                    continue;
                }
            }
        }

        return result;

    }

    /**
     * Returns the range of values in the domain (x-values) of a dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The range of values (possibly <code>null</code>).
     */
    public static Range findDomainBounds(XYDataset dataset) {
        return findDomainBounds(dataset, true);
    }

    /**
     * Returns the range of values in the domain (x-values) of a dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param includeInterval  determines whether or not the x-interval is taken
     *                         into account (only applies if the dataset is an
     *                         {@link IntervalXYDataset}).
     *
     * @return The range of values (possibly <code>null</code>).
     */
    public static Range findDomainBounds(XYDataset dataset, 
                                         boolean includeInterval) {

        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }

        Range result = null;
        // if the dataset implements DomainInfo, life is easier
        if (dataset instanceof DomainInfo) {
            DomainInfo info = (DomainInfo) dataset;
            result = info.getDomainBounds(includeInterval);
        }
        else {
            result = iterateDomainBounds(dataset, includeInterval);
        }
        return result;
        
    }

    /**
     * Iterates over the items in an {@link XYDataset} to find
     * the range of x-values. 
     *  
     * @param dataset  the dataset (<code>null</code> not permitted).
     * 
     * @return The range (possibly <code>null</code>).
     */
    public static Range iterateDomainBounds(XYDataset dataset) {
        return iterateDomainBounds(dataset, true);
    }

    /**
     * Iterates over the items in an {@link XYDataset} to find
     * the range of x-values. 
     *  
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param includeInterval  a flag that determines, for an IntervalXYDataset,
     *                         whether the x-interval or just the x-value is 
     *                         used to determine the overall range.
     *   
     * @return The range (possibly <code>null</code>).
     */
    public static Range iterateDomainBounds(XYDataset dataset, 
                                            boolean includeInterval) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");   
        }
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        double lvalue;
        double uvalue;
        if (includeInterval && dataset instanceof IntervalXYDataset) {
            IntervalXYDataset intervalXYData = (IntervalXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = intervalXYData.getStartXValue(series, item);
                    uvalue = intervalXYData.getEndXValue(series, item);
                    minimum = Math.min(minimum, lvalue);
                    maximum = Math.max(maximum, uvalue);
                }
            }
        }
        else {
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = dataset.getXValue(series, item);
                    uvalue = lvalue;
                    minimum = Math.min(minimum, lvalue);
                    maximum = Math.max(maximum, uvalue);
                }
            }
        }
        if (minimum > maximum) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }
    
    /**
     * Returns the range of values in the range for the dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The range (possibly <code>null</code>).
     */
    public static Range findRangeBounds(CategoryDataset dataset) {
        return findRangeBounds(dataset, true);
    }
    
    /**
     * Returns the range of values in the range for the dataset.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     * 
     * @return The range (possibly <code>null</code>).
     */
    public static Range findRangeBounds(CategoryDataset dataset, 
                                        boolean includeInterval) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        Range result = null;
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            result = info.getRangeBounds(includeInterval);
        }
        else {
            result = iterateCategoryRangeBounds(dataset, includeInterval);
        }
        return result;
    }
    
    /**
     * Returns the range of values in the range for the dataset.  This method
     * is the partner for the {@link #findDomainBounds(XYDataset)} method.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The range (possibly <code>null</code>).
     */
    public static Range findRangeBounds(XYDataset dataset) {
        return findRangeBounds(dataset, true);
    }
    
    /**
     * Returns the range of values in the range for the dataset.  This method
     * is the partner for the {@link #findDomainBounds(XYDataset)} method.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     * 
     *
     * @return The range (possibly <code>null</code>).
     */
    public static Range findRangeBounds(XYDataset dataset, 
                                        boolean includeInterval) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        Range result = null;
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            result = info.getRangeBounds(includeInterval);
        }
        else {
            result = iterateXYRangeBounds(dataset);
        }
        return result;
    }
    
    /**
     * Iterates over the data item of the category dataset to find
     * the range bounds.
     * 
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param includeInterval  a flag that determines whether or not the
     *                         y-interval is taken into account.
     * 
     * @return The range (possibly <code>null</code>).
     */
    public static Range iterateCategoryRangeBounds(CategoryDataset dataset, 
            boolean includeInterval) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        boolean interval = includeInterval 
                           && dataset instanceof IntervalCategoryDataset;
        int rowCount = dataset.getRowCount();
        int columnCount = dataset.getColumnCount();
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                Number lvalue;
                Number uvalue;
                if (interval) {
                    IntervalCategoryDataset icd 
                        = (IntervalCategoryDataset) dataset;
                    lvalue = icd.getStartValue(row, column);
                    uvalue = icd.getEndValue(row, column);
                }
                else {
                    lvalue = dataset.getValue(row, column);
                    uvalue = lvalue;
                }
                if (lvalue != null) {
                    minimum = Math.min(minimum, lvalue.doubleValue());
                }
                if (uvalue != null) {
                    maximum = Math.max(maximum, uvalue.doubleValue());
                }
            }
        }
        if (minimum == Double.POSITIVE_INFINITY) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }
    
    /**
     * Iterates over the data item of the xy dataset to find
     * the range bounds.
     * 
     * @param dataset  the dataset (<code>null</code> not permitted).
     * 
     * @return The range (possibly <code>null</code>).
     */
    public static Range iterateXYRangeBounds(XYDataset dataset) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        for (int series = 0; series < seriesCount; series++) {
            int itemCount = dataset.getItemCount(series);
            for (int item = 0; item < itemCount; item++) {
                double lvalue;
                double uvalue;
                if (dataset instanceof IntervalXYDataset) {
                    IntervalXYDataset intervalXYData 
                        = (IntervalXYDataset) dataset;
                    lvalue = intervalXYData.getStartYValue(series, item);
                    uvalue = intervalXYData.getEndYValue(series, item);
                }
                else if (dataset instanceof OHLCDataset) {
                    OHLCDataset highLowData = (OHLCDataset) dataset;
                    lvalue = highLowData.getLowValue(series, item);
                    uvalue = highLowData.getHighValue(series, item);
                }
                else {
                    lvalue = dataset.getYValue(series, item);
                    uvalue = lvalue;
                }
                if (!Double.isNaN(lvalue)) {
                    minimum = Math.min(minimum, lvalue);
                }
                if (!Double.isNaN(uvalue)) {     
                    maximum = Math.max(maximum, uvalue);
                }
            }
        }
        if (minimum == Double.POSITIVE_INFINITY) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }

    /**
     * Finds the minimum domain (or X) value for the specified dataset.  This 
     * is easy if the dataset implements the {@link DomainInfo} interface (a 
     * good idea if there is an efficient way to determine the minimum value).
     * Otherwise, it involves iterating over the entire data-set.
     * <p>
     * Returns <code>null</code> if all the data values in the dataset are 
     * <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The minimum value (possibly <code>null</code>).
     */
    public static Number findMinimumDomainValue(XYDataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        Number result = null;
        // if the dataset implements DomainInfo, life is easy
        if (dataset instanceof DomainInfo) {
            DomainInfo info = (DomainInfo) dataset;
            return new Double(info.getDomainLowerBound(true));
        }
        else {
            double minimum = Double.POSITIVE_INFINITY;
            int seriesCount = dataset.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {

                    double value;
                    if (dataset instanceof IntervalXYDataset) {
                        IntervalXYDataset intervalXYData 
                            = (IntervalXYDataset) dataset;
                        value = intervalXYData.getStartXValue(series, item);
                    }
                    else {
                        value = dataset.getXValue(series, item);
                    }
                    if (!Double.isNaN(value)) {
                        minimum = Math.min(minimum, value);
                    }

                }
            }
            if (minimum == Double.POSITIVE_INFINITY) {
                result = null;
            }
            else {
                result = new Double(minimum);
            }
        }

        return result;
    }
    
    /**
     * Returns the maximum domain value for the specified dataset.  This is 
     * easy if the dataset implements the {@link DomainInfo} interface (a good 
     * idea if there is an efficient way to determine the maximum value).  
     * Otherwise, it involves iterating over the entire data-set.  Returns 
     * <code>null</code> if all the data values in the dataset are 
     * <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The maximum value (possibly <code>null</code>).
     */
    public static Number findMaximumDomainValue(XYDataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        Number result = null;
        // if the dataset implements DomainInfo, life is easy
        if (dataset instanceof DomainInfo) {
            DomainInfo info = (DomainInfo) dataset;
            return new Double(info.getDomainUpperBound(true));
        }

        // hasn't implemented DomainInfo, so iterate...
        else {
            double maximum = Double.NEGATIVE_INFINITY;
            int seriesCount = dataset.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {

                    double value;
                    if (dataset instanceof IntervalXYDataset) {
                        IntervalXYDataset intervalXYData 
                            = (IntervalXYDataset) dataset;
                        value = intervalXYData.getEndXValue(series, item);
                    }
                    else {
                        value = dataset.getXValue(series, item);
                    }
                    if (!Double.isNaN(value)) {
                        maximum = Math.max(maximum, value);
                    }
                }
            }
            if (maximum == Double.NEGATIVE_INFINITY) {
                result = null;
            }
            else {
                result = new Double(maximum);
            }

        }
        
        return result;
    }

    /**
     * Returns the minimum range value for the specified dataset.  This is 
     * easy if the dataset implements the {@link RangeInfo} interface (a good
     * idea if there is an efficient way to determine the minimum value).  
     * Otherwise, it involves iterating over the entire data-set.  Returns 
     * <code>null</code> if all the data values in the dataset are 
     * <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The minimum value (possibly <code>null</code>).
     */
    public static Number findMinimumRangeValue(CategoryDataset dataset) {

        // check parameters...
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }

        // work out the minimum value...
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            return new Double(info.getRangeLowerBound(true));
        }

        // hasn't implemented RangeInfo, so we'll have to iterate...
        else {
            double minimum = Double.POSITIVE_INFINITY;
            int seriesCount = dataset.getRowCount();
            int itemCount = dataset.getColumnCount();
            for (int series = 0; series < seriesCount; series++) {
                for (int item = 0; item < itemCount; item++) {
                    Number value;
                    if (dataset instanceof IntervalCategoryDataset) {
                        IntervalCategoryDataset icd 
                            = (IntervalCategoryDataset) dataset;
                        value = icd.getStartValue(series, item);
                    }
                    else {
                        value = dataset.getValue(series, item);
                    }
                    if (value != null) {
                        minimum = Math.min(minimum, value.doubleValue());
                    }
                }
            }
            if (minimum == Double.POSITIVE_INFINITY) {
                return null;
            }
            else {
                return new Double(minimum);
            }

        }

    }

    /**
     * Returns the minimum range value for the specified dataset.  This is 
     * easy if the dataset implements the {@link RangeInfo} interface (a good
     * idea if there is an efficient way to determine the minimum value).  
     * Otherwise, it involves iterating over the entire data-set.  Returns 
     * <code>null</code> if all the data values in the dataset are 
     * <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The minimum value (possibly <code>null</code>).
     */
    public static Number findMinimumRangeValue(XYDataset dataset) {

        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }

        // work out the minimum value...
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            return new Double(info.getRangeLowerBound(true));
        }

        // hasn't implemented RangeInfo, so we'll have to iterate...
        else {
            double minimum = Double.POSITIVE_INFINITY;
            int seriesCount = dataset.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {

                    double value;
                    if (dataset instanceof IntervalXYDataset) {
                        IntervalXYDataset intervalXYData 
                            = (IntervalXYDataset) dataset;
                        value = intervalXYData.getStartYValue(series, item);
                    }
                    else if (dataset instanceof OHLCDataset) {
                        OHLCDataset highLowData = (OHLCDataset) dataset;
                        value = highLowData.getLowValue(series, item);
                    }
                    else {
                        value = dataset.getYValue(series, item);
                    }
                    if (!Double.isNaN(value)) {
                        minimum = Math.min(minimum, value);
                    }

                }
            }
            if (minimum == Double.POSITIVE_INFINITY) {
                return null;
            }
            else {
                return new Double(minimum);
            }

        }

    }

    /**
     * Returns the maximum range value for the specified dataset.  This is easy
     * if the dataset implements the {@link RangeInfo} interface (a good idea 
     * if there is an efficient way to determine the maximum value).  
     * Otherwise, it involves iterating over the entire data-set.  Returns 
     * <code>null</code> if all the data values are <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The maximum value (possibly <code>null</code>).
     */
    public static Number findMaximumRangeValue(CategoryDataset dataset) {

        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }

        // work out the minimum value...
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            return new Double(info.getRangeUpperBound(true));
        }

        // hasn't implemented RangeInfo, so we'll have to iterate...
        else {

            double maximum = Double.NEGATIVE_INFINITY;
            int seriesCount = dataset.getRowCount();
            int itemCount = dataset.getColumnCount();
            for (int series = 0; series < seriesCount; series++) {
                for (int item = 0; item < itemCount; item++) {
                    Number value;
                    if (dataset instanceof IntervalCategoryDataset) {
                        IntervalCategoryDataset icd 
                            = (IntervalCategoryDataset) dataset;
                        value = icd.getEndValue(series, item);
                    }
                    else {
                        value = dataset.getValue(series, item);
                    }
                    if (value != null) {
                        maximum = Math.max(maximum, value.doubleValue());
                    }
                }
            }
            if (maximum == Double.NEGATIVE_INFINITY) {
                return null;
            }
            else {
                return new Double(maximum);
            }

        }

    }

    /**
     * Returns the maximum range value for the specified dataset.  This is 
     * easy if the dataset implements the {@link RangeInfo} interface (a good 
     * idea if there is an efficient way to determine the maximum value).  
     * Otherwise, it involves iterating over the entire data-set.  Returns 
     * <code>null</code> if all the data values are <code>null</code>.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The maximum value (possibly <code>null</code>).
     */
    public static Number findMaximumRangeValue(XYDataset dataset) {

        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }

        // work out the minimum value...
        if (dataset instanceof RangeInfo) {
            RangeInfo info = (RangeInfo) dataset;
            return new Double(info.getRangeUpperBound(true));
        }

        // hasn't implemented RangeInfo, so we'll have to iterate...
        else  {

            double maximum = Double.NEGATIVE_INFINITY;
            int seriesCount = dataset.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double value;
                    if (dataset instanceof IntervalXYDataset) {
                        IntervalXYDataset intervalXYData 
                            = (IntervalXYDataset) dataset;
                        value = intervalXYData.getEndYValue(series, item);
                    }
                    else if (dataset instanceof OHLCDataset) {
                        OHLCDataset highLowData = (OHLCDataset) dataset;
                        value = highLowData.getHighValue(series, item);
                    }
                    else {
                        value = dataset.getYValue(series, item);
                    }
                    if (!Double.isNaN(value)) {
                        maximum = Math.max(maximum, value);
                    }
                }
            }
            if (maximum == Double.NEGATIVE_INFINITY) {
                return null;
            }
            else {
                return new Double(maximum);
            }

        }

    }

    /**
     * Returns the minimum and maximum values for the dataset's range 
     * (y-values), assuming that the series in one category are stacked.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The range (<code>null</code> if the dataset contains no values).
     */
    public static Range findStackedRangeBounds(CategoryDataset dataset) {
        return findStackedRangeBounds(dataset, 0.0);
    }

    /**
     * Returns the minimum and maximum values for the dataset's range 
     * (y-values), assuming that the series in one category are stacked.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param base  the base value for the bars.
     *
     * @return The range (<code>null</code> if the dataset contains no values).
     */
    public static Range findStackedRangeBounds(CategoryDataset dataset, 
            double base) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        Range result = null;
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int categoryCount = dataset.getColumnCount();
        for (int item = 0; item < categoryCount; item++) {
            double positive = base;
            double negative = base;
            int seriesCount = dataset.getRowCount();
            for (int series = 0; series < seriesCount; series++) {
                Number number = dataset.getValue(series, item);
                if (number != null) {
                    double value = number.doubleValue();
                    if (value > 0.0) {
                        positive = positive + value;
                    }
                    if (value < 0.0) {
                        negative = negative + value;  
                        // '+', remember value is negative
                    }
                }
            }
            minimum = Math.min(minimum, negative);
            maximum = Math.max(maximum, positive);
        }
        if (minimum <= maximum) {
            result = new Range(minimum, maximum);
        }
        return result;

    }

    /**
     * Returns the minimum and maximum values for the dataset's range 
     * (y-values), assuming that the series in one category are stacked.
     *
     * @param dataset  the dataset.
     * @param map  a structure that maps series to groups.
     *
     * @return The value range (<code>null</code> if the dataset contains no 
     *         values).
     */
    public static Range findStackedRangeBounds(CategoryDataset dataset,
                                               KeyToGroupMap map) {
    
        Range result = null;
        if (dataset != null) {
            
            // create an array holding the group indices...
            int[] groupIndex = new int[dataset.getRowCount()];
            for (int i = 0; i < dataset.getRowCount(); i++) {
                groupIndex[i] = map.getGroupIndex(
                    map.getGroup(dataset.getRowKey(i))
                );   
            }
            
            // minimum and maximum for each group...
            int groupCount = map.getGroupCount();
            double[] minimum = new double[groupCount];
            double[] maximum = new double[groupCount];
            
            int categoryCount = dataset.getColumnCount();
            for (int item = 0; item < categoryCount; item++) {
                double[] positive = new double[groupCount];
                double[] negative = new double[groupCount];
                int seriesCount = dataset.getRowCount();
                for (int series = 0; series < seriesCount; series++) {
                    Number number = dataset.getValue(series, item);
                    if (number != null) {
                        double value = number.doubleValue();
                        if (value > 0.0) {
                            positive[groupIndex[series]] 
                                 = positive[groupIndex[series]] + value;
                        }
                        if (value < 0.0) {
                            negative[groupIndex[series]] 
                                 = negative[groupIndex[series]] + value;
                                 // '+', remember value is negative
                        }
                    }
                }
                for (int g = 0; g < groupCount; g++) {
                    minimum[g] = Math.min(minimum[g], negative[g]);
                    maximum[g] = Math.max(maximum[g], positive[g]);
                }
            }
            for (int j = 0; j < groupCount; j++) {
                result = Range.combine(
                    result, new Range(minimum[j], maximum[j])
                );
            }
        }
        return result;

    }

    /**
     * Returns the minimum value in the dataset range, assuming that values in
     * each category are "stacked".
     *
     * @param dataset  the dataset.
     *
     * @return The minimum value.
     */
    public static Number findMinimumStackedRangeValue(CategoryDataset dataset) {

        Number result = null;
        if (dataset != null) {
            double minimum = 0.0;
            int categoryCount = dataset.getRowCount();
            for (int item = 0; item < categoryCount; item++) {
                double total = 0.0;

                int seriesCount = dataset.getColumnCount();
                for (int series = 0; series < seriesCount; series++) {
                    Number number = dataset.getValue(series, item);
                    if (number != null) {
                        double value = number.doubleValue();
                        if (value < 0.0) {
                            total = total + value;  
                            // '+', remember value is negative
                        }
                    }
                }
                minimum = Math.min(minimum, total);

            }
            result = new Double(minimum);
        }
        return result;

    }

    /**
     * Returns the maximum value in the dataset range, assuming that values in
     * each category are "stacked".
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The maximum value (possibly <code>null</code>).
     */
    public static Number findMaximumStackedRangeValue(CategoryDataset dataset) {

        Number result = null;

        if (dataset != null) {
            double maximum = 0.0;
            int categoryCount = dataset.getColumnCount();
            for (int item = 0; item < categoryCount; item++) {
                double total = 0.0;
                int seriesCount = dataset.getRowCount();
                for (int series = 0; series < seriesCount; series++) {
                    Number number = dataset.getValue(series, item);
                    if (number != null) {
                        double value = number.doubleValue();
                        if (value > 0.0) {
                            total = total + value;
                        }
                    }
                }
                maximum = Math.max(maximum, total);
            }
            result = new Double(maximum);
        }

        return result;

    }

    /**
     * Returns the minimum and maximum values for the dataset's range,
     * assuming that the series are stacked.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * 
     * @return The range ([0.0, 0.0] if the dataset contains no values).
     */
    public static Range findStackedRangeBounds(TableXYDataset dataset) {
        return findStackedRangeBounds(dataset, 0.0);
    }
    
    /**
     * Returns the minimum and maximum values for the dataset's range,
     * assuming that the series are stacked, using the specified base value.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param base  the base value.
     * 
     * @return The range (<code>null</code> if the dataset contains no values).
     */
    public static Range findStackedRangeBounds(TableXYDataset dataset, 
                                               double base) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        double minimum = base;
        double maximum = base;
        for (int itemNo = 0; itemNo < dataset.getItemCount(); itemNo++) {
            double positive = base;
            double negative = base;
            int seriesCount = dataset.getSeriesCount();
            for (int seriesNo = 0; seriesNo < seriesCount; seriesNo++) {
                double y = dataset.getYValue(seriesNo, itemNo);
                if (!Double.isNaN(y)) {
                    if (y > 0.0) {
                        positive += y;
                    }
                    else {
                        negative += y;
                    }
                }
            }
            if (positive > maximum) {
                maximum = positive;
            } 
            if (negative < minimum) {
                minimum = negative;
            } 
        }
        if (minimum <= maximum) {
            return new Range(minimum, maximum);
        }
        else {
            return null;   
        }
    }
    
    /**
     * Calculates the total for the y-values in all series for a given item
     * index.
     * 
     * @param dataset  the dataset.
     * @param item  the item index.
     * 
     * @return The total.
     * 
     * @since 1.0.5
     */
    public static double calculateStackTotal(TableXYDataset dataset, int item) {
        double total = 0.0;
        int seriesCount = dataset.getSeriesCount();
        for (int s = 0; s < seriesCount; s++) {
            double value = dataset.getYValue(s, item);
            if (!Double.isNaN(value)) {
                total = total + value;
            }
        }
        return total;
    }

    /**
     * Calculates the range of values for a dataset where each item is the 
     * running total of the items for the current series.
     * 
     * @param dataset  the dataset (<code>null</code> not permitted).
     * 
     * @return The range.
     * 
     * @see #findRangeBounds(CategoryDataset)
     */
    public static Range findCumulativeRangeBounds(CategoryDataset dataset) {
        
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        
        boolean allItemsNull = true; // we'll set this to false if there is at 
                                     // least one non-null data item... 
        double minimum = 0.0;
        double maximum = 0.0;
        for (int row = 0; row < dataset.getRowCount(); row++) {
            double runningTotal = 0.0;
            for (int column = 0; column < dataset.getColumnCount() - 1; 
                 column++) {
                Number n = dataset.getValue(row, column);
                if (n != null) {
                    allItemsNull = false;
                    double value = n.doubleValue();
                    runningTotal = runningTotal + value;
                    minimum = Math.min(minimum, runningTotal);
                    maximum = Math.max(maximum, runningTotal);
                }
            }    
        }
        if (!allItemsNull) {
            return new Range(minimum, maximum);
        }
        else {
            return null;
        }
        
    }

}
