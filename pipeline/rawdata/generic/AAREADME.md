--------------------------------------------------------------------------------
# H1
How to process model deliveries

These instructions assume there is a redmine issue for delivering some model
file(s). The issue is referred to below as <redmine-XXXX>. The delivery will
be associated with a set of saavtk and sbmt code repository branches. Except
as explicitly noted, these steps should be performed while logged in as
yourself, not logged into the sbmt account.

The model data processing scripts include many time-consuming steps, and
there are many possible causes of errors. The scripts and functions described
below are designed so that they may safely be invoked more than once in the
event of partial execution. In most cases, steps that were already completed
successfully will be skipped when a script is re-invoked. Moreover, the UNIX
rsync utility is used to copy files, so even steps that *do* repeat will
generally run faster the second time. 

1. Referring to redmine issue <redmine-XXXX>, inspect the delivered data in
the relevant /project/sbmtpipeline/deliveries*/<body>/<date>/<model>
directory. Confirm that the content matches the description, and that the
description has all the information needed. Work with the provider of the
data to resolve/clarify any discrepancies.

2. Assuming all is well, create and cd into a processing directory:
/project/sbmtpipeline/rawdata/<body>/<redmine-XXXX>

3. Copy the script build-client.sh from the directory
/project/sbmtpipeline/rawdata/start-up into the new processing directory and
use it to clone and install the correct version of the code for <redmine-XXXX>.
Make a copy and do not just invoke the script from the start-up directory. To
save time and disk space, if processing several deliveries in a batch that use
the same branches of the client code, you could also do this step once for the
first delivery, and then create symbolic links to the built saavtk and sbmt
directories for the other deliveries.

4. Copy the scripts sbmt/pipeline/rawdata/generic/processDelivery.sh and
sbmt/pipeline/rawdata/generic/deployDelivery.sh to the processing directory.
Do not just edit them and use them in place in the source tree. This is so that
the top-level processing directory serves as a self-contained archive of how
<redmine-XXXX> was processed.

5. Edit the local processDelivery.sh script to tailor it to this delivery.
This script archives the delivered files into the rawdata processing
directory (your current directory), and then processes these files, placing
the results in the corresponding
/project/sbmtpipeline/processed/<body>/<redmine-XXXX> directory. Use the
functions defined in sbmt/pipeline/rawdata/generic/dataProcessingFunctions.sh
to perform all the necessary steps for the given delivery, for example,
copying files or directories, processing plate colorings, DTMs, sumfiles,
infofiles, images, thumbnails etc.

6. Similarly, edit the deployDelivery.sh script to tailor it to this
delivery. This script deploys the processed files from
/project/sbmtpipeline/processed/<body>/<redmine-XXXX> to directories under
/project/sbmt2/sbmt/data/bodies/. As in the previous step, use functions
defined in dataProcessingFunctions.sh to perform the deployment. Except as
explicitly noted, deployDelivery.sh should not perform any additional
processing, i.e., it should just do basic things like copy files and create
symbolic links.
--------------------------------------------------------------------------------

7. Run the tailored command to import and process the delivery:

./runDataProcessing.sh processDelivery.sh

Monitor the progress and output of this command, check for errors, and review
the log files created in the logs directory. The scripts in
sbmt/pipeline/rawdata/generic are designed so that they may be invoked more
than once in the event of partial execution, and when re-started they will (in
general) skip steps that were successfully completed previously. If/when the
process completes without error, review the output files in the
/project/sbmtpipeline/processed/<body>/<redmine-XXXX> directory to ensure the
delivery was processed correctly. *It is especially important to ensure that
this step finished 100% successfully before proceeding to the next step.*

8. Log in as sbmt, and run the tailored command to deploy the delivery:

./runDataProcessing.sh deployDelivery.sh

Monitor the progress and output of this command, check for errors, and review
the log files created in the logs directory. Take corrective actions and
restart the script as necessary. If/when the process completes without error,
review the output files in the directory
/project/sbmt2/sbmt/data/bodies/<body>/<redmine-XXXX> to ensure the delivery
was deployed correctly.

9. The delivered model/code should at this point be visible if you now
launch a test client, i.e., using the "SmallBodyMappingToolAPL - Test"
run configuration in Eclipse.