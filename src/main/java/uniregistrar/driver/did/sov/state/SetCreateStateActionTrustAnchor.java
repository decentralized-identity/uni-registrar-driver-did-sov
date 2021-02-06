package uniregistrar.driver.did.sov.state;

import uniregistrar.state.CreateState;
import uniregistrar.state.SetCreateStateAction;

public class SetCreateStateActionTrustAnchor {

	private SetCreateStateActionTrustAnchor() {

	}

	public static boolean isStateActionTrustAnchor(CreateState createState) {

		return "trustanchor".equals(SetCreateStateAction.getStateAction(createState));
	}

	public static String getStateActionTrustAnchorDid(CreateState createState) {

		if (! isStateActionTrustAnchor(createState)) return null;
		return (String) createState.getDidState().get("did");
	}

	public static String getStateActionTrustAnchorVerkey(CreateState createState) {

		if (! isStateActionTrustAnchor(createState)) return null;
		return (String) createState.getDidState().get("verkey");
	}

	public static String getStateActionTrustAnchorUrl(CreateState createState) {

		if (! isStateActionTrustAnchor(createState)) return null;
		return (String) createState.getDidState().get("url");
	}

	public static void setStateActionTrustAnchor(CreateState createState, String did, String verkey, String url) {

		SetCreateStateAction.setStateAction(createState, "trustanchor");
		createState.getDidState().put("did", did);
		createState.getDidState().put("verkey", verkey);
		createState.getDidState().put("url", url);
	}
}
