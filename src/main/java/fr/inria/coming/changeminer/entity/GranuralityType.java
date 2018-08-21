package fr.inria.coming.changeminer.entity;
/**
 * 
 * @author Matias Martinez,  matias.martinez@inria.fr
 *
 */
public enum GranuralityType {
	CD {
		@Override
		public String ifType() {
			return "IF_STATEMENT";
		}
	},
	JDT{
		@Override
		public String ifType() {
			throw new UnsupportedOperationException();
		}
	},
	SPOON{
		@Override
		public String ifType() {
			return "if";
		}
	};

	public abstract String ifType();
}
