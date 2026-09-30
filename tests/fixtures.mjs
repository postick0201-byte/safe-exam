import {generateKeyPairSync} from 'node:crypto';
export function fakeFirebase(){
  process.env.ADMIN_PASSWORD='test-admin-password-only';process.env.SESSION_SECRET='test-only-session-secret-32-characters-long';process.env.PUBLIC_ORIGIN='http://localhost:4173';process.env.FIREBASE_DATABASE_URL='https://test-only.firebaseio.com';
  const {privateKey}=generateKeyPairSync('rsa',{modulusLength:2048});process.env.FIREBASE_SERVICE_ACCOUNT_JSON=JSON.stringify({client_email:'test@example.invalid',private_key:privateKey.export({type:'pkcs8',format:'pem'})});
  const data=new Map(),versions=new Map();let conflicts=0;
  const realFetch=globalThis.fetch;
  globalThis.fetch=async(url,opt={})=>{url=String(url);if(url==='https://oauth2.googleapis.com/token')return Response.json({access_token:'test-token',expires_in:3600});if(url.startsWith(process.env.FIREBASE_DATABASE_URL)){const path=new URL(url).pathname.slice(1,-5),version=versions.get(path)||0,etag='"'+version+'"';if(opt.method==='PUT'){if(conflicts>0){conflicts--;return new Response('',{status:412});}if(opt.headers['if-match']!==etag)return new Response('',{status:412});data.set(path,JSON.parse(opt.body));versions.set(path,version+1);return Response.json(data.get(path));}if(opt.method==='DELETE'){data.delete(path);return Response.json(null);}return Response.json(data.get(path)||null,{headers:{etag}});}return realFetch(url,opt);};
  return {data,setConflicts:n=>conflicts=n,restore:()=>globalThis.fetch=realFetch};
}
