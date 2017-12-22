include ${SBMTROOT}/config/Makefiles/Variables

# all = compile client java code into jar files in build directory
# install = installs near-apl.jar to lib directory as near.jar, and installs tools to bin directory.
# release = copies everything needed for a release to build directory and makes the zip files
# deploy = put the zip files out on the web server
# clean = delete the 'build' directory.

PUBLIC_VERSION := public
INTERNAL_VERSION := internal
VTK_VERSION := vtk6.3
PUBLIC_OUTPUT_DIR := ${SBMTROOT}/build/dist/public
INTERNAL_OUTPUT_DIR := ${SBMTROOT}/build/dist/internal

MYCLASSPATH = ${CLASSPATH}
OS := $(shell uname)

top_level_dir = ${SBMTROOT}
saavtk_dir = ${SAAVTKROOT}
saavtk_lib_dir = ${SAAVTKROOT}/lib
vtk_dir = ${saavtk_lib_dir}/${VTK_VERSION}
jogl_dir = ${saavtk_lib_dir}/jogl
jre_dir = /project/nearsdc/software/vtk_all_platforms

# Environment variables needed for 'ant' to run 
# setenv ANT_HOME /project/nearsdc/software/apache-ant/latest
# setenv JAVA_HOME /project/nearsdc/software/java/x86_64/latest
#

build_dir     := ${SBMTROOT}/build
BUILD_JAR_DIR := ${build_dir}/jar
NEARJAR       := ${BUILD_JAR_DIR}/near.jar
NEARAPLJAR    := ${BUILD_JAR_DIR}/near-apl.jar
SAAVTKJAR    := ${SAAVTKROOT}/build/jar/saavtk.jar

int_dist_dir := ${SBMTROOT}/build/dist/internal
pub_dist_dir := ${SBMTROOT}/build/dist/public

int_mac_dir   := ${int_dist_dir}/mac64
int_linux_dir := ${int_dist_dir}/linux64
int_win_dir   := ${int_dist_dir}/win64

int_mac_sbmt_dir   := ${int_mac_dir}/sbmt
int_linux_sbmt_dir := ${int_linux_dir}/sbmt
int_win_sbmt_dir   := ${int_win_dir}/sbmt

int_mac_sbmt_lib_dir   := ${int_mac_sbmt_dir}/lib
int_linux_sbmt_lib_dir := ${int_linux_sbmt_dir}/lib
int_win_sbmt_lib_dir   := ${int_win_sbmt_dir}/lib


int_mac64_jar   :=  ${int_mac_sbmt_lib_dir}/near.jar
int_linux64_jar :=  ${int_linux_sbmt_lib_dir}/near.jar
int_win64_jar   :=  ${int_win_sbmt_lib_dir}/near.jar
int_jars        := ${int_mac64_jar} ${int_linux64_jar} ${int_win64_jar}

pub_mac_dir   := ${pub_dist_dir}/mac64
pub_linux_dir := ${pub_dist_dir}/linux64
pub_win_dir   := ${pub_dist_dir}/win64

pub_mac_sbmt_dir   := ${pub_mac_dir}/sbmt
pub_linux_sbmt_dir := ${pub_linux_dir}/sbmt
pub_win_sbmt_dir   := ${pub_win_dir}/sbmt

pub_mac_sbmt_lib_dir   := ${pub_mac_sbmt_dir}/lib
pub_linux_sbmt_lib_dir := ${pub_linux_sbmt_dir}/lib
pub_win_sbmt_lib_dir   := ${pub_win_sbmt_dir}/lib

pub_mac64_jar   :=  ${pub_mac_sbmt_lib_dir}/near.jar
pub_linux64_jar :=  ${pub_linux_sbmt_lib_dir}/near.jar
pub_win64_jar   :=  ${pub_win_sbmt_lib_dir}/near.jar

int_jars        := ${int_mac64_jar} ${int_linux64_jar} ${int_win64_jar}
pub_jars        := ${pub_mac64_jar} ${pub_linux64_jar} ${pub_win64_jar}

mac_zip   :=  ${RELEASE}-macosx-x64.zip
linux_zip :=  ${RELEASE}-linux-x64.zip
win_zip   :=  ${RELEASE}-windows-x64.zip

int_mac_zip   :=  ${int_mac_dir}/${mac_zip}
int_linux_zip :=  ${int_linux_dir}/${linux_zip}
int_win_zip   :=  ${int_win_dir}/${win_zip}

pub_mac_zip   :=  ${pub_mac_dir}/${mac_zip}
pub_linux_zip :=  ${pub_linux_dir}/${linux_zip}
pub_win_zip   :=  ${pub_win_dir}/${win_zip}

int_zips      := ${int_mac_zip} ${int_linux_zip} ${int_win_zip}
pub_zips      := ${pub_mac_zip} ${pub_linux_zip} ${pub_win_zip}

all_java_files := ${SBMTROOT}/src/edu/jhuapl/sbmt/*/*.java


.SUFFIXES: .jar .zip

${most_supported_targets} :

all : ${NEARJAR} ${NEARAPLJAR}

install : ${NEARAPLJAR} tools
		@echo $@: "Installing near-apl.jar to ${LIBROOT} as near.jar" 
		cp ${NEARAPLJAR} ${LIBROOT}/near.jar

release : install ${int_zips} ${pub_zips}
ifeq ($(OS), Darwin)
	@echo "Copying mac libraries to lib/."
	rsync -av --exclude='*.txt' ${int_mac_sbmt_lib_dir}/ ${LIBROOT}
else
	@echo "Copying linux libraries to lib/."
	rsync -av --exclude='*.txt' ${int_linux_sbmt_lib_dir}/ ${LIBROOT}
endif

deploy : ${int_zips} ${pub_zips}
ifeq (,$(findstring,SNAPSHOT,${RELEASE}))
		@chmod 664 ${int_mac_zip}
		@chmod 664 ${int_linux_zip}
		@chmod 664 ${int_win_zip}
		@chmod 664 ${pub_mac_zip}
		@chmod 664 ${pub_linux_zip}
		@chmod 664 ${pub_win_zip}
		@mkdir -p ${INTERNAL_DEPLOY_RELEASE_DIR}
		@mkdir -p ${PUBLIC_DEPLOY_RELEASE_DIR}
		cp ${int_mac_zip} ${INTERNAL_DEPLOY_RELEASE_DIR}
		cp ${int_linux_zip} ${INTERNAL_DEPLOY_RELEASE_DIR}
		cp ${int_win_zip} ${INTERNAL_DEPLOY_RELEASE_DIR}
		cp ${pub_mac_zip} ${PUBLIC_DEPLOY_RELEASE_DIR}
		cp ${pub_linux_zip} ${PUBLIC_DEPLOY_RELEASE_DIR}
		cp ${pub_win_zip} ${PUBLIC_DEPLOY_RELEASE_DIR}
else
		@echo "Current release is just a SNAPSHOT and can not be deployed."
endif

clean :
	ant -f ${SBMTROOT}/build.xml clean

bare : clean

${NEARJAR} : ${all_java_files}
	ant -f ${SBMTROOT}/build.xml -DRELEASE="${RELEASE}" -DMISSION=${MISSION} jar

${NEARAPLJAR} : ${all_java_files}
	ant -f ${SBMTROOT}/build.xml -DRELEASE="${RELEASE}" -DMISSION=${MISSION} jarapl


# Generate Run Scripts
tools :
		@echo $@: "Installing all tools to ${BINROOT}"
		@${GENERATE_TOOLS}
 
# Note this uses near-apl.jar, and creating 'runsbmt' is same as linux64 but different than win64
# and generate_runsbmt.sh takes the INTERNAL parameters.
${int_mac_zip} : ${NEARAPLJAR}
		@echo "Making $@"
		@mkdir -p ${int_mac_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${int_mac_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${int_mac_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${int_mac_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${int_mac_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt-natives-macosx-universal.jar ${int_mac_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${int_mac_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all-natives-macosx-universal.jar ${int_mac_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${int_mac_sbmt_lib_dir}
		cp ${NEARAPLJAR} ${int_mac_sbmt_lib_dir}/near.jar
		cp -R ${vtk_dir}/mac64 ${int_mac_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-mac64 ${int_mac_sbmt_dir}
		rm -rf ${int_mac_sbmt_dir}/jre
		mv ${int_mac_sbmt_dir}/jre-mac64 ${int_mac_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${int_mac_sbmt_lib_dir}
		${SBMTROOT}/config/generate_runsbmt.sh ${INTERNAL_OUTPUT_DIR} ${INTERNAL_VERSION} ${MISSION}
		(cd ${int_mac_dir} ; zip -q -r ${mac_zip} sbmt)

# Note this uses near-apl.jar, and creating 'runsbmt' is same as mac64 but different than win64
# and generate_runsbmt.sh takes the INTERNAL parameters.
${int_linux_zip} : ${NEARAPLJAR}
		@echo "Making $@"
		@mkdir -p ${int_linux_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${int_linux_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${int_linux_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${int_linux_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${int_linux_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt-natives-linux-amd64.jar ${int_linux_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${int_linux_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all-natives-linux-amd64.jar ${int_linux_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${int_linux_sbmt_lib_dir}
		cp ${NEARAPLJAR} ${int_linux_sbmt_lib_dir}/near.jar
		cp -R ${vtk_dir}/linux64 ${int_linux_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-linux64 ${int_linux_sbmt_dir}
		rm -rf ${int_linux_sbmt_dir}/jre
		mv ${int_linux_sbmt_dir}/jre-linux64 ${int_linux_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${int_linux_sbmt_lib_dir}
		${SBMTROOT}/config/generate_runsbmt.sh ${INTERNAL_OUTPUT_DIR} ${INTERNAL_VERSION} ${MISSION}
		(cd ${int_linux_dir} ; zip -q -r ${linux_zip} sbmt)

# Note this uses near-apl.jar, and creating 'runsbmt' script is different here.
${int_win_zip} : ${NEARAPLJAR}
		@echo "Making $@"
		@mkdir -p ${int_win_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${int_win_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${int_win_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${int_win_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${int_win_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${int_win_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${int_win_sbmt_lib_dir}
		cp ${NEARAPLJAR} ${int_win_sbmt_lib_dir}/near.jar
		cp -R ${vtk_dir}/win64 ${int_win_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-win64 ${int_win_sbmt_dir}
		rm -rf ${int_win_sbmt_dir}/jre
		mv ${int_win_sbmt_dir}/jre-win64 ${int_win_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${int_win_sbmt_lib_dir}
		ifeq (${MISSION}, "133314b")
			cp ${jre_dir}/runsbmt_hayabusa.exe ${int_win_sbmt_dir}/runsbmt.exe
		else
			cp ${jre_dir}/runsbmt.exe ${int_win_sbmt_dir}
		@chmod +x ${int_win_sbmt_dir}/runsbmt.exe
		(cd ${int_win_dir} ; zip -q -r ${win_zip} sbmt)

 
# Note this uses near.jar, and creating 'runsbmt' is same as linux64 but different than win64
# and generate_runsbmt.sh takes the PUBLIC parameters.
${pub_mac_zip} : ${NEARJAR}
		@echo "Making $@"
		@mkdir -p ${pub_mac_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${pub_mac_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${pub_mac_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${pub_mac_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${pub_mac_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${pub_mac_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${pub_mac_sbmt_lib_dir}
		cp ${NEARJAR} ${pub_mac_sbmt_lib_dir}
		cp -R ${vtk_dir}/mac64 ${pub_mac_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-mac64 ${pub_mac_sbmt_dir}
		rm -rf ${pub_mac_sbmt_dir}/jre
		mv ${pub_mac_sbmt_dir}/jre-mac64 ${pub_mac_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${pub_mac_sbmt_lib_dir}
		${SBMTROOT}/config/generate_runsbmt.sh ${PUBLIC_OUTPUT_DIR} ${PUBLIC_VERSION}
		(cd ${pub_mac_dir} ; zip -q -r ${mac_zip} sbmt)

# Note this uses near.jar, and creating 'runsbmt' is same as mac64 but different than win64
# and generate_runsbmt.sh takes the PUBLIC parameters.
${pub_linux_zip} : ${NEARJAR} 
		@echo "Making $@"
		@mkdir -p ${pub_linux_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${pub_linux_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${pub_linux_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${pub_linux_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${pub_linux_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${pub_linux_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt-natives-linux-amd64.jar ${pub_linux_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all-natives-linux-amd64.jar ${pub_linux_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${pub_linux_sbmt_lib_dir}
		cp ${NEARJAR} ${pub_linux_sbmt_lib_dir}
		cp -R ${vtk_dir}/linux64 ${pub_linux_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-linux64 ${pub_linux_sbmt_dir}
		rm -rf ${pub_linux_sbmt_dir}/jre
		mv ${pub_linux_sbmt_dir}/jre-linux64 ${pub_linux_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${pub_linux_sbmt_lib_dir}
		${SBMTROOT}/config/generate_runsbmt.sh ${PUBLIC_OUTPUT_DIR} ${PUBLIC_VERSION}
		(cd ${pub_linux_dir} ; zip -q -r ${linux_zip} sbmt)

# Note this uses near.jar, and creating the 'runsbmt' script is different here.
${pub_win_zip} : ${NEARJAR}
		@echo "Making $@"
		@mkdir -p ${pub_win_sbmt_lib_dir}
		cp ${top_level_dir}/lib/*.jar ${pub_win_sbmt_lib_dir}
		cp ${saavtk_dir}/lib/*.jar ${pub_win_sbmt_lib_dir}
		cp ${vtk_dir}/*.jar ${pub_win_sbmt_lib_dir}
		cp ${jogl_dir}/gluegen-rt.jar ${pub_win_sbmt_lib_dir}
		cp ${jogl_dir}/jogl-all.jar ${pub_win_sbmt_lib_dir}
		cp ${SAAVTKJAR} ${pub_win_sbmt_lib_dir}
		cp ${NEARJAR} ${pub_win_sbmt_lib_dir}
		cp -R ${vtk_dir}/win64 ${pub_win_sbmt_lib_dir}
		cp -R ${jre_dir}/jre-win64 ${pub_win_sbmt_dir}
		rm -rf ${pub_win_sbmt_dir}/jre
		mv ${pub_win_sbmt_dir}/jre-win64 ${pub_win_sbmt_dir}/jre
		cp ${top_level_dir}/src/edu/jhuapl/sbmt/data/license.txt ${pub_win_sbmt_lib_dir}
		ifeq (${MISSION}, "133314b")
			cp ${jre_dir}/runsbmt_hayabusa.exe ${pub_win_sbmt_dir}/runsbmt.exe
		else
			cp ${jre_dir}/runsbmt.exe ${pub_win_sbmt_dir}
		@chmod +x ${pub_win_sbmt_dir}/runsbmt.exe
		(cd ${pub_win_dir} ; zip -q -r ${win_zip} sbmt)

include ${SBMTROOT}/config/Makefiles/Master
