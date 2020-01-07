package fr.inria.coming.codefeatures.sec;

/**
 * 
 * @author Matias Martinez
 *
 */
public class VulInstance {

	String commit;
	String project;
	String CVE;
	String type;
	String CWE;
	String CWE_type;

	public VulInstance(String commit, String project, String cVE, String type) {
		super();
		this.commit = commit;
		this.project = project;
		this.CVE = cVE;
		this.type = type;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getCVE() {
		return CVE;
	}

	public void setCVE(String cVE) {
		CVE = cVE;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "VulInstance [commit=" + commit + ", project=" + project + ", CVE=" + CVE + ", type=" + type + "]";
	}

	public String getCWE() {
		return CWE;
	}

	public void setCWE(String cWE) {
		CWE = cWE;
	}

	public String getCWEType() {
		return CWE_type;
	}

	public void setCWEType(String cWE_type) {
		CWE_type = cWE_type;
	}
}
