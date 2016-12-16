var println = function (args) {
    try {
        logger.info(args);
    } catch (e) {
        java.lang.System.out.println(args);
    }
}

scriptEngine.addGlobalVariable({
    "println": println,
    "alert": println
});
