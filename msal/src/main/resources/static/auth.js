const msalConfig = {
  auth: {
      clientId: msalClientId,
      authority: 'https://login.microsoftonline.com/' + msalTenantId,
  },
  cache: {
    cacheLocation: "localStorage",
    storeAuthStateInCookie: false,
  },
  graph: {
  }
};

const pca = new msal.PublicClientApplication(msalConfig);

async function signIn() {
  const loginRequest = {
      scopes: [
      ]
  };

  try {
    const response = await pca.loginPopup(loginRequest);
    console.log('Utilisateur connecté !', response);

    const msalData = {
      tenantId: response.account.idTokenClaims.tid,
      clientId: response.account.idTokenClaims.aud,
      token: response.idToken
    };
    
    const callApi = await fetch('http://localhost:8080/login', {
      method: 'POST',
      headers: {
          'Content-Type': 'application/json',
      },
      body: JSON.stringify(msalData)
    })

    if (callApi.status === 200) {
      console.log('Utilisateur connecté à l\'application !');
    } else {
      console.log('Erreur lors de la connexion à l\'application :', callApi);
    }
  } catch (error) {
    console.log('Erreur lors de la connexion :', error);
    window.location.href = '/';
  }
}

document.addEventListener('DOMContentLoaded', function() {
  const signInButton = document.getElementById('signInButton');
  signInButton.addEventListener('click', signIn);
});
