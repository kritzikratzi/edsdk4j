package edsdk;

import java.lang.reflect.Field;

public class SLRHelpers {
	/**
	 * Tries to find name of an error code. 
	 * 
	 * @param errorCode
	 * @return
	 */
	public static String toString( int errorCode ){
		Field[] fields = CanonSDK.class.getFields();
		for( Field field : fields ){
			try {
				if( field.getType().toString().equals( "int" ) && field.getInt( CanonSDK.class ) == errorCode ){
					return field.getName(); 
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return "unknown error code"; 
	}

}
