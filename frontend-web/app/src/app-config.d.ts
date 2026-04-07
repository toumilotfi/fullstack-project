interface AppRuntimeConfig {
  apiUrl?: string;
  wsUrl?: string;
}

declare global {
  var __appConfig: AppRuntimeConfig | undefined;

  interface Window {
    __appConfig?: AppRuntimeConfig;
  }
}

export {};
