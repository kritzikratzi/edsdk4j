#define __MACOS__
#include "EDSDK.h"
#include "EDSDKErrors.h"
#include "EDSDKTypes.h"
#include <iostream>

using namespace std;

/**
 * Simple EDSDK camera test
 */
int main()
{
    EdsError error = EDS_ERR_OK;
    EdsCameraListRef cameraList = NULL;
    EdsUInt32 count = 0;
    EdsDeviceInfo deviceInfo;
    bool isSDKLoaded=false;

    // Initialization of SDK
    error = EdsInitializeSDK();

    //Acquisition of camera list
    if(error == EDS_ERR_OK)
    {
        isSDKLoaded = true;
        error = EdsGetCameraList(&cameraList);
        // Get number of cameras
        if(error == EDS_ERR_OK)
        {
            error = EdsGetChildCount(cameraList, &count);
            if(count == 0)
            {
                error = EDS_ERR_DEVICE_NOT_FOUND;
            } else {
                cout << "# of cameras found is: " << count << "\n";
            }
        }
    }
    if(cameraList != NULL) {
        EdsRelease(cameraList);
        cameraList = NULL;
    }

    return 0;
}