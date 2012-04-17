#include "lidardata.h"
#include "util.h"
#include "SpiceUsr.h"
#include <fstream>
#include <iostream>
#include <math.h>
#include <stdlib.h>


LidarData::LidarData()
{
}

LidarTrack LidarData::loadTrack(const string &filename,
                                BodyType bodyType,
                                bool convertToJ2000,
                                double startTime,
                                double stopTime)
{
    ifstream fin(filename.c_str());

    LidarTrack referenceTrajectory;
    char utc[24];

    if (fin.is_open())
    {
        string line;
        while (getline(fin, line))
        {
            LidarPoint p;
            vector<string> tokens = split(line);

            if (bodyType == ITOKAWA)
            {
                string time = tokens[1].c_str();
                utc2et_c(time.c_str(), &p.time);

                if (startTime < stopTime)
                {
                    if (p.time < startTime || p.time >= stopTime)
                        continue;
                }

                p.range = atof(tokens[2].c_str());

                if (tokens.size() > 3)
                {
                    p.scpos[0] = atof(tokens[3].c_str());
                    p.scpos[1] = atof(tokens[4].c_str());
                    p.scpos[2] = atof(tokens[5].c_str());
                    p.targetpos[0] = atof(tokens[6].c_str());
                    p.targetpos[1] = atof(tokens[7].c_str());
                    p.targetpos[2] = atof(tokens[8].c_str());

                    if (convertToJ2000)
                    {
                        // transform to J2000
                        const char* frame = "IAU_ITOKAWA";
                        const char* ref = "J2000";
                        double i2bmat[3][3];
                        pxform_c(frame, ref, p.time, i2bmat);
                        mxv_c(i2bmat, p.scpos, p.scpos);
                    }

                    p.boredir[0] = p.targetpos[0] - p.scpos[0];
                    p.boredir[1] = p.targetpos[1] - p.scpos[1];
                    p.boredir[2] = p.targetpos[2] - p.scpos[2];
                    vhat_c(p.boredir, p.boredir);
                }
            }
            else if (bodyType == EROS)
            {
                // The first 2 lines have as the first character either an 'L' or 'l'.
                // Ignore them
                if (line[0] == 'L' || line[0] == 'l')
                    continue;

                int noise;
                double sclon;
                double sclat;
                double scrdst;
                sscanf(line.c_str(), "%*s %*s %*s %*s %s %*s %*s %d %lf %lf %lf",
                       utc,
                       &noise,
                       &sclon,
                       &sclat,
                       &scrdst);

                if (noise == 1)
                    continue;

                utc2et_c(utc, &p.time);

                scrdst /= 1000.0;
                latrec_c(scrdst, sclon*M_PI/180.0, sclat*M_PI/180.0, p.scpos);

                // transform to J2000
                if (convertToJ2000)
                {
                    const char* frame = "IAU_EROS";
                    const char* ref = "J2000";
                    double i2bmat[3][3];
                    pxform_c(frame, ref, p.time, i2bmat);
                    mxv_c(i2bmat, p.scpos, p.scpos);
                }

                p.boredir[0] = p.targetpos[0] - p.scpos[0];
                p.boredir[1] = p.targetpos[1] - p.scpos[1];
                p.boredir[2] = p.targetpos[2] - p.scpos[2];
                vhat_c(p.boredir, p.boredir);
            }

            referenceTrajectory.push_back(p);
        }

        fin.close();
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }

    return referenceTrajectory;
}

void LidarData::saveTrack(const string &filename,
                          const LidarTrack& track,
                          BodyType bodyType,
                          bool convertFromJ2000)
{
    ofstream fout(filename.c_str());

    if (fout.is_open())
    {
        for (unsigned int i = 0; i<track.size(); ++i)
        {
            const LidarPoint& p = track[i];

            if (bodyType == ITOKAWA)
            {
                char strTime[32];
                et2utc_c(p.time, "ISOC", 3, 32, strTime);

                if(convertFromJ2000)
                {
                    // transform to body fixed
                    double newPos[3];
                    const char* ref = "J2000";
                    const char* frame = "IAU_ITOKAWA";
                    double i2bmat[3][3];
                    pxform_c(ref, frame, p.time, i2bmat);
                    mxv_c(i2bmat, p.scpos, newPos);

                    fout << 0 << " " << strTime << " 0 " << newPos[0] << " " << newPos[1] << " " << newPos[2] << " 0 0 0\n";
                }
                else
                {
                    fout << 0 << " " << strTime << " 0 " << p.scpos[0] << " " << p.scpos[1] << " " << p.scpos[2] << " 0 0 0\n";
                }
            }
//            else if (bodyType == EROS)
//            {
//                char strTime[32];
//                et2utc_c(p.time, "ISOC", 3, 32, strTime);
//
//                // transform to body fixed
//                double newPos[3];
//                const char* ref = "J2000";
//                const char* frame = "IAU_EROS";
//                double i2bmat[3][3];
//                pxform_c(ref, frame, p.time, i2bmat);
//                mxv_c(i2bmat, p.scpos, newPos);
//
//                double sclon;
//                double sclat;
//                double scrdst;
//                reclat_c(p.scpos, &scrdst, &sclon, &sclat);
//                fout << "0 0 0 0 "
//                     << strTime
//                     << " 0 0 0 "
//                     << (sclon * 180.0 / M_PI) << " "
//                     << (sclat * 180.0 / M_PI) << " "
//                     << (1000.0 * scrdst)
//                     << " 0 0 0 0 0 0\n";
//            }
        }

        fout.close();
    }
    else
    {
        cerr << "Error: Unable to open file '" << filename << "'" << endl;
        exit(1);
    }
}
