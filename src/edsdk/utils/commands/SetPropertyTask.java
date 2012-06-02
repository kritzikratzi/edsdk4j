package edsdk.utils.commands;

import edsdk.EdSdkLibrary;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

public class SetPropertyTask extends CanonTask<Boolean> {

	private long property;
	private long value;
	
	public SetPropertyTask( long property, long value ){
		this.property = property; 
		this.value = value; 
	}
	
	@Override
	public void run() {
		int result = CanonUtils.setPropertyData( camera.getEdsCamera(),  property, value );
		System.out.println( "Set property: " + CanonUtils.propertyIdToString( property ) + " = " + value + ", result = " + CanonUtils.toString( result ) ); 
		setResult( result == EdSdkLibrary.EDS_ERR_OK ); 
	}

}
