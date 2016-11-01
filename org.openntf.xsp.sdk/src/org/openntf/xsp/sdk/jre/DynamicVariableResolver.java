package org.openntf.xsp.sdk.jre;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.openntf.xsp.sdk.preferences.OldPreferences;
import org.openntf.xsp.sdk.preferences.XPagesSDKPreferences;

public class DynamicVariableResolver implements IDynamicVariableResolver {
	public String resolveValue(IDynamicVariable variable, String arg1) throws CoreException {
//		String ref = OldPreferences.resolveConstant(variable.getName());
//		if (XPagesSDKPreferences.NOTES_INSTALL.equals(ref))
//			return OldPreferences.getNotesInstall();
//		if (OldPreferences.RCP_BASE.equals(ref))
//			try {
//				return OldPreferences.getRcpBase();
//			} catch (Exception e) {
//				throw new CoreException(Status.CANCEL_STATUS);
//			}
//		if (OldPreferences.RCP_DATA.equals(ref))
//			return OldPreferences.getRcpData();
//		if (OldPreferences.RCP_TARGET.equals(ref))
//			return OldPreferences.getRcpTarget();
//		if (XPagesSDKPreferences.DOMINO_INSTALL.equals(ref))
//			return OldPreferences.getDominoInstall();
//		if (OldPreferences.DOMRCP_BASE.equals(ref))
//			try {
//				return OldPreferences.getDomRcpBase();
//			} catch (Exception e) {
//				throw new CoreException(Status.CANCEL_STATUS);
//			}
//		if (OldPreferences.DOMRCP_DATA.equals(ref))
//			return OldPreferences.getDomRcpData();
//		if (OldPreferences.DOMRCP_TARGET.equals(ref))
//			return OldPreferences.getDomRcpTarget();
//		if (OldPreferences.DOMSHARED_TARGET.equals(ref))
//			return OldPreferences.getDomSharedTarget();
//
//		if (OldPreferences.DOTSRCP_BASE.equals(ref))
//			try {
//				return OldPreferences.getDotsRcpBase();
//			} catch (Exception e) {
//				throw new CoreException(Status.CANCEL_STATUS);
//			}
//		if (OldPreferences.DOTSRCP_DATA.equals(ref))
//			return OldPreferences.getDotsRcpData();
//		if (OldPreferences.DOTSRCP_TARGET.equals(ref))
//			return OldPreferences.getDotsRcpTarget();
//		if (OldPreferences.DOTSSHARED_TARGET.equals(ref))
//			return OldPreferences.getDotsSharedTarget();

		return "";
	}
}
