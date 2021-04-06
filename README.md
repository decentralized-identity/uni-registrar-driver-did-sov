![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png)

# Universal Registrar Driver: sov

This is a [Universal Registrar](https://github.com/decentralized-identity/universal-registrar/) driver for **did:sov** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [DID Method Specification](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html)

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalregistrar/driver-did-sov
docker run -p 9080:9080 universalregistrar/driver-did-sov
```

## Driver Environment Variables

The driver recognizes the following environment variables:

* `uniregistrar_driver_did_sov_libIndyPath`: The path to the Indy SDK library.
* `uniregistrar_driver_did_sov_poolConfigs`: A semi-colon-separated list of Indy network names and pool configuration files. The default network is `_`.
* `uniregistrar_driver_did_sov_poolVersions`: A semi-colon-separated list of Indy network names and pool protocol versions. The default network is `_`.
* `uniregistrar_driver_did_sov_walletName`: The name of the Indy wallet.
* `uniregistrar_driver_did_sov_trustAnchorSeed`: The seed of the trust anchor which will submit the DID registration transactions.

## Driver Input Options

```
{
    "network": "builder"
}
```

* `network`: The name of the network where a DID should be registered. Values depend on `poolConfigs` environment variable, but are typically: `_` (mainnet), `staging`, `builder`, `danube`

## Driver Output Metadata

```
{
    "network": "danube",
    "poolVersion": 2,
    "submitterDid": "V4SGRU86Z58d6TV7PBUe6f"
}
```

* `network`: The name of the network where the DID was registered. Values depend on `poolConfigs` environment variable, but are typically: `_` (mainnet), `staging`, `builder`, `danube`
* `poolVersion`: The version of the network where the DID was registered.
* `submitterDid`: The DID of the transaction author which submitted the DID registration.
