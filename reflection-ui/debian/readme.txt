DEBIAN INTEGRATION
------------------
Build commands:
	export DEBFULLNAME="OTK Software"
	export DEBEMAIL="dotxyteam@yahoo.fr"
	
	mh_make
	
	export VERSION=`cat debian/changelog | grep UNRELEASED |  grep -P '\d+(\.\d+)+' -o`
	export MAVEN_VERSION=`cat pom.xml | grep -A10 "<project " | grep "<version>" |  grep -P '\d+(\.\d+)+' -o`
	if [ "$VERSION" == "$MAVEN_VERSION" ]; then echo "Current version=$VERSION"; else echo "---- WARNING: Version mismatch: DEBIAN=$VERSION and MAVEN=$MAVEN_VERSION ----"; fi
	
	echo 'tar cfz ../reflection-ui_'$VERSION'.orig.tar.gz src *.xml *.txt' > debian/orig-tar.sh; chmod +x debian/orig-tar.sh; debian/orig-tar.sh
	echo 'extend-diff-ignore = "(debian|tmp|tools|\.classpath|\.settings|\.project)"' > debian/source/options 
	
	cp pom.xml pom.beforeDebianBuild.xml;\
	BUILD_DIR="$HOME/tmp/reflection-ui";\
	rm -rf "$BUILD_DIR";\
	mkdir "$BUILD_DIR";\
	mv ../reflection-ui_$VERSION.orig.tar.gz "$BUILD_DIR/..";\
	cp -r debian "$BUILD_DIR";\
	cd "$BUILD_DIR";\
	tar xfz ../reflection-ui_$VERSION.orig.tar.gz;\
	debuild;\
	cd -;\
	cp pom.beforeDebianBuild.xml pom.xml
	
Cleaning command:
	cd $HOME/tmp/reflection-ui; rm ../*reflection-ui*.*; cd -
	debuild clean
	

