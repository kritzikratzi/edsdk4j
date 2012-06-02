package edsdk.utils.commands;

import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

public class GetPropertyTask extends CanonTask<Long> {

	private long property;
	
	public GetPropertyTask( long property ){
		this.property = property; 
	}
	
	@Override
	public void run() {
		long result = CanonUtils.getPropertyData( camera.getEdsCamera(),  property ); 
		setResult( result ); 
	}

}
