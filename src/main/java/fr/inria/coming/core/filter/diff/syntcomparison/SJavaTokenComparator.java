package fr.inria.coming.core.filter.diff.syntcomparison;

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.dom.rewrite.TokenScanner;

/**
 * A comparator for Java tokens.
 */
public class SJavaTokenComparator implements Fragmentable {
	

  private static final boolean DEBUG= false;

  private final String fText;
  private int fCount;
  private int[] fStarts;
  private int[] fLengths;
  private String[] fTokens;


  /**
   * Creates a token comparator for the given string.
   *
   * @param text the text to be tokenized
   * @param textTokenComparatorFactory a factory to create text token comparators
   */
  public SJavaTokenComparator(String text) {


    fText= text;

    int length= fText.length();
    fStarts= new int[length];
    fLengths= new int[length];
    fTokens= new String[length];
    fCount= 0;

    IScanner scanner= ToolFactory.createScanner(true, false, false, false); // returns comments & whitespace
    scanner.setSource(fText.toCharArray());
    int endPos= 0;
    try {
      int tokenType;
      while ((tokenType= scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        int start= scanner.getCurrentTokenStartPosition();
        int end= scanner.getCurrentTokenEndPosition()+1;
        // Comments are treated as a single token (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=78063)
        if (!TokenScanner.isComment(tokenType)) {
          recordTokenRange(start, end - start);
        }
        endPos= end;
      }
    } catch (InvalidInputException ex) {
      // We couldn't parse part of the input. Fall through and make the rest a single token
    }
    // Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=13907
    if (endPos < length) {
      recordTokenRange(endPos, length - endPos);
    }
  }
  
  @Override
  public String getFragment(int i) {
    return getToken(i);
  }


  public String getToken(int i) {
    return fTokens[i];
  }

  /**
   * Records the given token range.
   *
   * @param start of the token
   * @param length length of the token
   * @since 3.3
   */
  private void recordTokenRange(int start, int length) {
    fStarts[fCount]= start;
    fLengths[fCount]= length;
    String token = fText.substring(start, start + length);
    fTokens[fCount]= token;
    if (DEBUG)
     {
      System.out.println(fCount + " (" + start + "-" + length + ")>" + token + "<"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    fCount++;
  }


  /**
   * Returns the length of the token that
   * initiates the given comment type.
   *
   * @param tokenType
   * @return the length of the token that start a comment
   * @since 3.3
   */
  private static int getCommentStartTokenLength(int tokenType) {
    if (tokenType == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
      return 3;
    } else if (tokenType == ITerminalSymbols.TokenNameStringLiteral) {
      return 1;
    } else {
      return 2;
    }
  }

  /**
   * Returns the number of tokens in the string.
   *
   * @return number of token in the string
   */
  @Override
  public int getRangeCount() {
    return fCount;
  }

  /* (non Javadoc)
   * see ITokenComparator.getTokenStart
   */
  public int getTokenStart(int index) {
    if (index >= 0 && index < fCount) {
      return fStarts[index];
    }
    if (fCount > 0) {
      return fStarts[fCount-1] + fLengths[fCount-1];
    }
    return 0;
  }

  /* (non Javadoc)
   * see ITokenComparator.getTokenLength
   */
  public int getTokenLength(int index) {
    if (index < fCount) {
      return fLengths[index];
    }
    return 0;
  }

  /**
   * Returns <code>true</code> if a token given by the first index
   * matches a token specified by the other <code>IRangeComparator</code> and index.
   *
   * @param thisIndex the number of the token within this range comparator
   * @param other the range comparator to compare this with
   * @param otherIndex the number of the token within the other comparator
   * @return <code>true</code> if the token are equal
   */
  @Override
  public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
    if (other != null && getClass() == other.getClass()) {
      SJavaTokenComparator tc= (SJavaTokenComparator) other;  // safe cast
      int thisLen= getTokenLength(thisIndex);
      int otherLen= tc.getTokenLength(otherIndex);
      if (thisLen == otherLen) {
        return fText.regionMatches(false, getTokenStart(thisIndex), tc.fText, tc.getTokenStart(otherIndex), thisLen);
      }
    }
    return false;
  }

  /**
   * Aborts the comparison if the number of tokens is too large.
   *
   * @param length a number on which to base the decision whether to return
   *  <code>true</code> or <code>false</code>
   * @param maxLength another number on which to base the decision whether to return
   *  <code>true</code> or <code>false</code>
   * @param other the other <code>IRangeComparator</code> to compare with
   * @return <code>true</code> to abort a token comparison
   */
  @Override
  public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {

    if (getRangeCount() < 50 || other.getRangeCount() < 50) {
      return false;
    }

    if (maxLength < 100) {
      return false;
    }

    if (length < 100) {
      return false;
    }

    if (maxLength > 800) {
      return true;
    }

    if (length < maxLength / 4) {
      return false;
    }

    return true;
  }
	
	@Override
	public String toString() {
		String ret = "";
		for (int i = 0; i < this.fTokens.length; i++)
			ret += fTokens[i] + "\n";
		return ret;
	}
}
