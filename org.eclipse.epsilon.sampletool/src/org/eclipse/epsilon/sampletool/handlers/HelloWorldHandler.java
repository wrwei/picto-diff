package org.eclipse.epsilon.sampletool.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/** <b>Warning</b> : 
As explained in <a href="http://wiki.eclipse.org/Eclipse4/RCP/FAQ#Why_aren.27t_my_handler_fields_being_re-injected.3F">this wiki page</a>, it is not recommended to define @Inject fields in a handler. <br/><br/>
<b>Inject the values in the @Execute methods</b>
*/
public class HelloWorldHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s) {
		try {
		EolModule m = new EolModule();
		m.parse("return 'Hello world';");
		Object o = m.execute();
		
		
		MessageDialog.openInformation(s, "E4 Information Dialog", o+"");
		}
		catch (Exception ex) {
			System.out.println(ex);
		}
	}


}
