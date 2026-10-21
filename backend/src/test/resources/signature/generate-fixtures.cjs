// Generates ADR-0016 interoperability fixtures: test certificates and seals created with node-forge,
// exactly as the e.firma component signs (SHA-256 digest, pkey.sign(md) -> RSASSA-PKCS1-v1_5).
// Test data only; no real supplier certificates or keys.
// Usage (repo root): NODE_PATH=frontend/node_modules node backend/src/test/resources/signature/generate-fixtures.cjs
const forge = require('node-forge');
const fs = require('fs');
const path = require('path');

const UTF8 = forge.asn1.Type.UTF8;
const attr = (type, value) => ({ type, value, valueTagClass: UTF8 });
const RFC = '2.5.4.45', CN = '2.5.4.3', NAME = '2.5.4.41', ORG = '2.5.4.10', COUNTRY = '2.5.4.6';

const variants = {
  'legal-entity': [attr(CN, 'EMPRESA EJEMPLO SA DE CV'), attr(NAME, 'EMPRESA EJEMPLO SA DE CV'),
    attr(ORG, 'EMPRESA EJEMPLO SA DE CV'), attr(COUNTRY, 'MX'), attr(RFC, 'AAA010101AAA')],
  'legal-entity-with-representative': [attr(CN, 'EMPRESA APODERADA SA DE CV'), attr(COUNTRY, 'MX'),
    attr(RFC, 'AAA010101AAA/HEGT7610034S2')],
  'individual': [attr(CN, 'JOSÉ PÉREZ NÚÑEZ'), attr(NAME, 'JOSE PEREZ NUNEZ'), attr(RFC, 'HEGT7610034S2')],
  'missing-rfc': [attr(CN, 'SIN RFC SA DE CV')],
  'empty-first-rfc-segment': [attr(CN, 'SEGMENTO VACIO SA'), attr(RFC, '/HEGT7610034S2')],
  'short-rfc': [attr(CN, 'RFC CORTO SA'), attr(RFC, 'AAA0101')],
  'name-41-before-3': [attr(NAME, 'NOMBRE DEL OID 41'), attr(CN, 'NOMBRE DEL OID 3'), attr(RFC, 'AAA010101AAA')],
  'name-10-first': [attr(ORG, 'ORGANIZACION OID 10'), attr(NAME, 'NOMBRE DEL OID 41'), attr(RFC, 'AAA010101AAA')],
  'missing-name': [attr(COUNTRY, 'MX'), attr(RFC, 'AAA010101AAA')],
  'empty-first-name': [attr(CN, ''), attr(NAME, 'NOMBRE POSTERIOR'), attr(RFC, 'AAA010101AAA')],
};

const fixtures = {};
for (const [name, subject] of Object.entries(variants)) {
  const keys = forge.pki.rsa.generateKeyPair(2048);
  const cert = forge.pki.createCertificate();
  cert.publicKey = keys.publicKey;
  cert.serialNumber = '01';
  cert.validity.notBefore = new Date('2026-01-01T00:00:00Z');
  cert.validity.notAfter = new Date('2030-01-01T00:00:00Z');
  cert.setSubject(subject);
  cert.setIssuer([attr(CN, 'AUTORIDAD DE PRUEBA')]);
  cert.sign(keys.privateKey, forge.md.sha256.create());

  const md = forge.md.sha256.create();
  md.update('Temporal', 'utf8');
  const seal = keys.privateKey.sign(md);

  const der = forge.asn1.toDer(forge.pki.certificateToAsn1(cert)).getBytes();
  fixtures[name] = {
    certificado: forge.util.encode64(der),
    certificadoPem: forge.pki.certificateToPem(cert),
    cadenaFirma: forge.util.encode64(seal),
  };
}
fs.writeFileSync(path.join(__dirname, 'fixtures.json'), JSON.stringify(fixtures, null, 2) + '\n');
console.log('fixtures:', Object.keys(fixtures).length);
