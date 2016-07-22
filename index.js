let React = require('react-native');
let { NativeModules, NativeAppEventEmitter } = React;

let TwilioRCT = NativeModules.Twilio;

let _eventHandlers = {
  deviceDidStartListening: new Map(),
  deviceDidStopListening: new Map(),
  deviceDidReceiveIncoming: new Map(),
  connectionDidFail: new Map(),
  connectionDidStartConnecting: new Map(),
  connectionDidConnect: new Map(),
  connectionDidDisconnect: new Map(),
  deviceReady: new Map(),
  deviceCreationFail: new Map(),
  deviceUpdated: new Map(),
};

let Twilio = {
  setSpeaker(enabled) {
    TwilioRCT.setSpeaker(enabled);
  },
  initWithTokenUrl(tokenUrl) {
    TwilioRCT.initWithTokenUrl(tokenUrl);
  },
  initWithToken(token) {
    TwilioRCT.initWithToken(token);
  },
  connect(params = {}) {
    TwilioRCT.connect(params);
  },
  disconnect() {
    TwilioRCT.disconnect();
  },
  accept() {
    TwilioRCT.accept();
  },
  reject() {
    TwilioRCT.reject();
  },
  ignore() {
    TwilioRCT.ignore();
  },
  setMuted(isMuted) {
    TwilioRCT.setMuted(isMuted);
  },
  addEventListener(type, handler) {
    _eventHandlers[type].set(handler, NativeAppEventEmitter.addListener(
      type, rtn => {
        handler(rtn);
      }
    ));
  },
  removeEventListener(type, handler) {
    if (!_eventHandlers[type].has(handler)) {
      return;
    }
    _eventHandlers[type].get(handler).remove();
    _eventHandlers[type].delete(handler);
  },
};

module.exports = Twilio;
