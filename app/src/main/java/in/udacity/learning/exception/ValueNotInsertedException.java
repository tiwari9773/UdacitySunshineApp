package in.udacity.learning.exception;

public class ValueNotInsertedException extends Exception {
	private static final long serialVersionUID = 1L;
	String strException = "";

	public ValueNotInsertedException() {
		super();
	}

	public ValueNotInsertedException(String strException) {
		super(strException);
		this.strException = strException+" --> "+getClass().getName();
	}

	@Override
	public String toString() {
		return (strException);
	}
}
