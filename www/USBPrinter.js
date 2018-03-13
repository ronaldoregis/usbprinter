var exec = require('cordova/exec');

exports.print = function (arg0, success, error) {
    exec(success, error, 'USBPrinter', 'print', [arg0]);
};
exports.sendText = function (arg0, success, error) {
    exec(success, error, 'USBPrinter', 'sendText', [arg0]);
};