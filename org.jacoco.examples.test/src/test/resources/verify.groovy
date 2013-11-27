File realBaseDir = new File(basedir, "../../../target/it/build");
assert new File(realBaseDir, "target/site/jacoco/index.html").exists();
assert !new File(realBaseDir, "target/site/jacoco-it/index.html").exists();
return true;