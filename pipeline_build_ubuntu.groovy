failure_job_list = []
node ("cicd_vm") {
   currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
   currentBuild.result = 'SUCCESS'
   try{

       stage ("Compile mvcriu and mvmalloc ${BUILD_LABEL} branch")
       {
          //build job: "pipeline_nightly_build_stage", parameters: [string(name: "build_branch", value: "${BUILD_LABEL}")]

               echo "ubuntu build"
               build job: "pipeline_nightly_build_ubuntu", parameters: [string(name: "build_branch", value: "${BUILD_LABEL}"),
               [$class: "LabelParameterValue", name: "node", label: "ubuntu"], string(name: "RHEL_VER", value: "${RHEL_VER}"),
               string(name: "Pipeline_name", value: "Pipeline_master"), string(name: 'criu_branch', value: "${criu_branch}")]

       }
       //stage ("Install basic workflow test")
       //{
       //  build job: 'pipeline_mvmalloc_nightly_install', parameters: [string(name: 'BUILD_LABEL', value: 'basic install test'),
       //   string(name: 'BUILD_LOCATION', value:  "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/${pkg_name}"),
        //  string(name: 'TEST_SUITE', value: 'test_fresh_install'), string(name: 'HOSTS_DAX_MAP', value: '10.0.0.102=/dev/dax0.0,/dev/dax1.0'),
        //  //string(name: 'TEST_SUITE', value: 'test_fresh_install'), string(name: 'HOSTS_DAX_MAP', value: '10.0.1.165=/dev/dax0.0,/dev/dax1.0'),
        //  string(name: 'USER_PW', value: 'memverge'), booleanParam(name: 'SKIP_NUMA_CTL', value: true), booleanParam(name: 'FORCE_CLEANUP', value: true),
        //  string(name: 'MVTEST_BRANCH', value: 'master'), booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       //}
       stage ("Functional redis test")
       {
           build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
        //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
           string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "run_redis_snapshot_functional_test"),
         //  string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
        //   string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/mvmm*x86_64.tgz"),
           string(name: "BUILD_LOCATION", value: "${BUILD_LOCATION}"),
           string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
         //  string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
           string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
           string(name: 'MVTEST_BRANCH', value: "${MVTEST_BRANCH}"),
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       }
       // stage ("Functional kdb test")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       // }
       // stage ("Functional kdb test, run as non sudo user, mvsnapd service")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0")
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "2"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
       //     booleanParam(name: 'runAsNoneSudo', value: true),booleanParam(name: 'start_mvmallocd_service', value: true),
       //     booleanParam(name: 'mvsnapd_service', value: true)]
       // }
       stage ("Functional redis test, Hugepage cache")
       {
           build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
        //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
           string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "run_redis_snapshot_functional_test"),
         //  string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
        //   string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/mvmm*x86_64.tgz"),
           string(name: "BUILD_LOCATION", value: "${BUILD_LOCATION}"),
           string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
         //  string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
           string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
           string(name: 'MVTEST_BRANCH', value: "${MVTEST_BRANCH}"),
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
           booleanParam(name: 'HugePageDram', value: true), booleanParam(name: 'RegularDram', value: false), string(name: 'DramCacheGB', value: '3')]
       }
       // stage ("Functional kdb test, regular cache")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
       //     booleanParam(name: 'HugePageDram', value: false), booleanParam(name: 'RegularDram', value: true), string(name: 'DramCacheGB', value: '3')]
       // }
       stage ("MVMCLI basic workflow")
       {
            build job: "pipeline_mvmalloc_nightly_mvmcli", parameters: [string(name: "BUILD_LABEL", value: "mvmcli"), string(name: "BUILD_DIR",
            value: "/memverge/automation/ubuntu/${BUILD_LABEL}_tmp/${RHEL_VER}"), string(name: "BUILD_DATE", value: "*"), string(name: "TEST_SUITE", value: "basic"),
            string(name: "KX_HOME", value: "/memverge/automation/q/l64"),string(name: 'HOSTS_DAX_MAP', value: '10.0.0.101=/dev/dax0.0,/dev/dax1.0'),
            string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"), string(name: "BUILD_PACKAGE_NAME", value: "${pkg_name}"),
            booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
        }
       //stage ("Package")
       //{
       //   echo "pkg rhel7"
       //   build job: 'pipeline_mvmalloc_nightly_pkg', parameters: [string(name: 'BUILD_BRANCH', value: "${BUILD_LABEL}"), string(name: "RHEL_VER", value: "${RHEL_VER}")]
       //   echo "pkg rhel8"
       //   build job: 'pipeline_mvmalloc_nightly_pkg', parameters: [string(name: 'BUILD_BRANCH', value: "${BUILD_LABEL}"), string(name: "RHEL_VER", value: "rhel8")]
       //
       //   echo "pkg both rhel7 and rhel8"
       //   build job: 'pipeline_pkg_rhel78_same_dir', parameters: [string(name: 'BUILD_BRANCH', value: "${BUILD_LABEL}")]
       //}
   }
   catch (e){
            echo e.getMessage()
            currentBuild.result = 'FAILURE'
   }
   finally {
            emailext body:
            "*Pipline Nightly build status--master*: ${currentBuild.result}\n\n" +
            "*Pipeline run overview* ${env.JOB_URL} \n\n" +
            "*Current run url* ${env.BUILD_URL}\n\n",
            subject: "Pipeline ubuntu Nightly Build", to: 'jack.pao@memverge.com'
            if("${SLACK_NOTIFICATION}" == 'true'){
              if(currentBuild.result == 'FAILURE')
                  sh '''
                  curl -X POST -H 'Content-type: application/json' --data '{"text":"Nightly build status--master *FAILURE* https://104.184.156.164:8888/view/Pipeline_view/job/Pipeline_master\nChangelog: http://niles.eng.memverge.com/automation/changelog/master/mvmalloc_changelog http://niles.eng.memverge.com/automation/changelog/master/mvcriu_changelog"}' \
                  https://hooks.slack.com/services/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/xxx;

                  '''
              else
                  sh '''
                  curl -X POST -H 'Content-type: application/json' --data '{"text":"Nightly build status--master *SUCCESS* https://104.184.156.164:8888/view/Pipeline_view/job/Pipeline_master\nChangelog: http://niles.eng.memverge.com/automation/changelog/master/mvmalloc_changelog http://niles.eng.memverge.com/automation/changelog/master/mvcriu_changelog"}' \
                  https://hooks.slack.com/services/T768GRL82/B01A5DJ2D7Z/dpVnbhpKiIJBNQih1vM0ZJB/xxxJ;

                  '''

            }
   }
}
