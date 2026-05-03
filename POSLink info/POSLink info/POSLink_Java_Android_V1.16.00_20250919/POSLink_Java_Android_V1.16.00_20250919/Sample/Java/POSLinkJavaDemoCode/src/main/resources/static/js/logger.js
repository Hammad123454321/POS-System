
define(function () {
    var logImpl = null
    var setLogger = function(impl) {
        logImpl = impl
    }
    var log = function(s) {
        logImpl(s)
    }

    return {
        setLogger: setLogger,
        log: log
    }
});