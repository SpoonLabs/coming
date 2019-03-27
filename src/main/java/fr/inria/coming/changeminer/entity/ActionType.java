package fr.inria.coming.changeminer.entity;
/**
 * Types of operations (The acronyms come from Actions)
 * @author Matias Martinez,  matias.martinez@inria.fr
 *
 *PD: The best name should be "Change type", but It should be confused with the enumeration of CD. 
 */
public enum ActionType {
	INS, DEL, UPD, MOV, PER, ANY, UNCHANGED, UNCHANGED_HIGH_PRIORITY;
	public boolean isUnchanged (){
	    return this.equals(UNCHANGED) || this.equals(UNCHANGED_HIGH_PRIORITY);
    }
}
