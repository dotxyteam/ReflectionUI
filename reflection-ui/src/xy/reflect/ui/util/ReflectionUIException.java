package xy.reflect.ui.util;

public class ReflectionUIException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	
	
	public ReflectionUIException() {
		this("Unexpected error");
	}

	public ReflectionUIException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionUIException(String message) {
		super(message);
	}

	public ReflectionUIException(Throwable cause) {
		this(cause.toString(), cause);
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public String toString() {
		return getMessage();
	}
	
	

}
