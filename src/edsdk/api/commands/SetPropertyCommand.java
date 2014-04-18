package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.utils.CanonUtils;

public class SetPropertyCommand extends CanonCommand<Boolean> {

	private long property;
	private long value;
	
	public SetPropertyCommand( long property, long value ){
		this.property = property; 
		this.value = value; 
	}
	
	@Override
	public void run() {
		int result = CanonUtils.setPropertyData( camera.getEdsCamera(),  property, value );
		int retries = 0; 
		while( result == EdSdkLibrary.EDS_ERR_DEVICE_BUSY && retries < 500 ){
			result = CanonUtils.setPropertyData( camera.getEdsCamera(), property, value );
			try{
				Thread.sleep( 10 );
				Thread.yield(); 
			}
			catch( Exception e ){
				setResult( false ); 
				return; 
			}
			
			retries ++; 
		}
		
		System.out.println( "Set property: " + CanonUtils.propertyIdToString( property ) + " = " + value + ", result = " + CanonUtils.toString( result ) + " after " + retries + " tries" ); 
		setResult( result == EdSdkLibrary.EDS_ERR_OK ); 
	}
}
