package uniregistrar.driver.did.sov;

import com.danubetech.libindy.IndyConnection;
import com.danubetech.libindy.IndyConnectionException;
import com.danubetech.libindy.IndyConnector;
import com.danubetech.libindy.LibIndyInitializer;
import foundation.identity.did.DIDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.sov.mode.internal.Create;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

import java.util.HashMap;
import java.util.Map;

public class DidSovDriver extends AbstractDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidSovDriver.class);

	private Map<String, Object> properties;

	private LibIndyInitializer libIndyInitializer;
	private IndyConnector indyConnector;

	public DidSovDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidSovDriver() {

		this(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<> ();

		try {

			String env_libIndyPath = System.getenv("uniregistrar_driver_did_sov_libIndyPath");
			String env_poolConfigs = System.getenv("uniregistrar_driver_did_sov_poolConfigs");
			String env_poolVersions = System.getenv("uniregistrar_driver_did_sov_poolVersions");
			String env_walletNames = System.getenv("uniregistrar_driver_did_sov_walletNames");
			String env_submitterDidSeeds = System.getenv("uniregistrar_driver_did_sov_submitterDidSeeds");
			String env_genesisTimestamps = System.getenv("uniregistrar_driver_did_sov_genesisTimestamps");

			if (env_libIndyPath != null) properties.put("libIndyPath", env_libIndyPath);
			if (env_poolConfigs != null) properties.put("poolConfigs", env_poolConfigs);
			if (env_poolVersions != null) properties.put("poolVersions", env_poolVersions);
			if (env_walletNames != null) properties.put("walletNames", env_walletNames);
			if (env_submitterDidSeeds != null) properties.put("submitterDidSeeds", env_submitterDidSeeds);
			if (env_genesisTimestamps != null) properties.put("genesisTimestamps", env_genesisTimestamps);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_libIndyPath = (String) this.getProperties().get("libIndyPath");

			this.setLibIndyInitializer(new LibIndyInitializer(
					prop_libIndyPath));

			String prop_poolConfigs = (String) this.getProperties().get("poolConfigs");
			String prop_poolVersions = (String) this.getProperties().get("poolVersions");
			String prop_walletNames = (String) this.getProperties().get("walletNames");
			String prop_submitterDidSeeds = (String) this.getProperties().get("submitterDidSeeds");
			String prop_genesisTimestamps = (String) this.getProperties().get("genesisTimestamps");

			this.setIndyConnector(new IndyConnector(
					prop_poolConfigs,
					prop_poolVersions,
					prop_walletNames,
					prop_submitterDidSeeds,
					prop_genesisTimestamps));
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public CreateState create(CreateRequest createRequest) throws RegistrationException {

		// init

		if (!this.getLibIndyInitializer().isInitialized()) {
			this.getLibIndyInitializer().initializeLibIndy();
			if (log.isInfoEnabled()) log.info("Successfully initialized libindy.");
		}

		// open indy connections

		if (!this.getIndyConnector().isOpened()) {
			try {
				this.getIndyConnector().openIndyConnections(true, true);
				if (log.isInfoEnabled()) log.info("Successfully opened Indy connections.");
			} catch (IndyConnectionException ex) {
				throw new RegistrationException("Cannot open Indy connections: " + ex.getMessage(), ex);
			}
		}

		// read options, secret, didDocument, jobId

		String network = createRequest.getOptions() == null ? null : (String) createRequest.getOptions().get("network");
		if (network == null || network.trim().isEmpty()) network = "_";

		String seed = createRequest.getSecret() == null ? null : (String) createRequest.getSecret().get("seed");

		DIDDocument didDocument = createRequest.getDidDocument();

		String jobId = createRequest.getJobId();

		// find Indy connection

		IndyConnection indyConnection = this.getIndyConnector().getIndyConnections().get(network);
		if (indyConnection == null) throw new RegistrationException("Unknown network: " + network);

		// create

		return Create.create(indyConnection, seed, didDocument);
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {
		throw new RegistrationException("Not implemented.");
	}

	@Override
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
		throw new RegistrationException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		Map<String, Object> properties = new HashMap<String, Object> (this.getProperties());
		if (properties.containsKey("trustAnchorSeed")) properties.put("trustAnchorSeed", ((String) properties.get("trustAnchorSeed")).replaceAll(".", "."));

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
		this.configureFromProperties();
	}

	public LibIndyInitializer getLibIndyInitializer() {
		return libIndyInitializer;
	}

	public void setLibIndyInitializer(LibIndyInitializer libIndyInitializer) {
		this.libIndyInitializer = libIndyInitializer;
	}

	public IndyConnector getIndyConnector() {
		return indyConnector;
	}

	public void setIndyConnector(IndyConnector indyConnector) {
		this.indyConnector = indyConnector;
	}
}
