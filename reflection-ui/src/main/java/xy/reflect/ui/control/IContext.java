package xy.reflect.ui.control;

public interface IContext {

	
	IContext NULL_CONTEXT = new IContext() {		
		@Override
		public String getIdentifier() {
			return "NULL_CONTEXT";
		}
	};

	public String getIdentifier();
}
