def basejobname = DEVICE + '-' + BUILD_TYPE
def BUILD_TREE = "/var/lib/jenkins/workspace/builder"

node {
	currentBuild.displayName = basejobname

	stage('Sync') {
		sh '''#!/bin/bash
		cd '''+BUILD_TREE+'''
		rm -rf .repo/local_manifests
		echo "Resetting current working tree...."
	        repo forall -c "git reset --hard" > /dev/null
		echo "Reset complete!"
	        repo forall -c "git clean -f -d"
	        repo sync -d -c --force-sync --no-tags --no-clone-bundle
		'''
	}
	stage('Clean') {
		sh '''#!/bin/bash
		cd '''+BUILD_TREE+'''
		make clean
		make clobber
		'''
	}
	stage('Build') {
		sh '''#!/bin/bash +e
		cd '''+BUILD_TREE+'''
		. build/envsetup.sh
		ccache -M 75G
		export USE_CCACHE=1
		export CCACHE_COMPRESS=1
		export FH_WEEKLY=true
		lunch fh_$DEVICE-$BUILD_TYPE
		mka bacon
		'''
	}
	stage('Upload') {
		sh '''#!/bin/bash
		set -e
		cd '''+BUILD_TREE+'''
		echo "Deploying artifacts..."
		gdrive upload '''+BUILD_TREE+'''/out/target/product/*/FireHound-*.zip -p 1UugE3Eb43arYnfn0muFvIkkDkbvj3NAr
		curl -s -X GET https://api.firehound.org/nodejs/api/gdrive-files > /dev/null
		echo "Synced $DEVICE build successfully!"
		'''
	}
}
