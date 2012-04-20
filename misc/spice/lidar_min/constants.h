#ifndef CONSTANTS_H
#define CONSTANTS_H

typedef enum BodyType
{
    ITOKAWA,
    EROS
} BodyType;

const char* const ITOKAWA_NAME = "ITOKAWA";
const char* const EROS_NAME = "EROS";

const char* const ITOKAWA_FRAME = "IAU_ITOKAWA";
const char* const EROS_FRAME = "IAU_EROS";

#endif // CONSTANTS_H
