File realBaseDir = new File(basedir, "../../../target/it-offline/build");
assert new File(realBaseDir, "target/site/jacoco/index.html").exists();
assert !new File(realBaseDir, "target/site/jacoco-it/index.html").exists();
assert new File(realBaseDir, "build.log").getText().contains(":restore-instrumented-classes");
return true;