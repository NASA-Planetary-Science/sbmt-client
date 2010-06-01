#include <stdlib.h>
#include <stdio.h>

/* This function converts a fortran string to a c string. A fortran
   string is space padded at the end whereas a c string is null
   terminated. This function replaces the first of the trailing spaces
   with a null character. There must be at least one trailing space
   for this function to work, otherwise an error is thrown.

   Inputs:
   
   str - the fortran string to convert. There must be at least one trailing space.
   size - the size of the string. This number includes the string as
          well as any space character padding at the end.

   Outputs:

   str - a null-terminated c string. The the original string is overwritten.
*/

void convertString(char* str, int size)
{
	int i = size - 1;
	if (size == 0)
    {
		fprintf(stderr, "ERROR: String is empty.");
		exit(1);
    }
	if (str[i] != ' ')
    {
		fprintf(stderr, "ERROR: Cannot convert to a c string since there is no ");
		fprintf(stderr, "trailing space.");
		exit(1);
    }
	
	--i;
	while(i >= 0)
    {
		if (str[i] != ' ')
		{
			str[i+1] = '\0';
			return;
		}
		--i;
    }
	
	/* If we reach here, that means the string is empty so just make the
	   first character null. */
	str[0] = '\0';
}
