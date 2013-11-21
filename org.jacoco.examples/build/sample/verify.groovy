assert new File(basedir, "target/site/jacoco/index.html").exists();
assert !new File(basedir, "target/site/jacoco-it/index.html").exists();
return true;
