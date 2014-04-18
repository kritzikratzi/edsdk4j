package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonUtils;

public class GetPropertyCommand extends CanonCommand<Long> {

	private long property;
	
	public GetPropertyCommand( long property ){
		this.property = property; 
	}
	
	@Override
	public void run() {
		long result = CanonUtils.getPropertyData( camera.getEdsCamera(),  property ); 
		setResult( result ); 
	}

}
