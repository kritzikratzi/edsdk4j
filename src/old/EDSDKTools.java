package old;

import java.lang.reflect.Field;

public class EDSDKTools {

	public static String toString( long errorCode ){
		Field[] fields = EDSDKConstants.class.getFields();
		for( Field field : fields ){
			try {
				if( field.getLong( EDSDKConstants.class ) == errorCode ){
					return field.getName() + " (errorcode " + errorCode + ")"; 
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return "unknown error code (errorcode " + errorCode + ")" ; 
	}
}
