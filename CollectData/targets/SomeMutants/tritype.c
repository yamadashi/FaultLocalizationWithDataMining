#include "tritype.h"

int tritype(int i, int j, int k)
{
    int trityp;
    if ((i == 0) || (j == 0) || (k == 0))
        trityp = 4;
    else
    {
        trityp = 0;
        if (i == j)
            trityp = i + 1; //mutant
        if (i >= k) //mutant
            trityp = trityp + 2;
        if (j == k)
            trityp = trityp + 3;
        if (trityp == 0)
        {
            if ((i + j <= k) || (j + k <= i) || (i + k <= j))
                trityp = 4;
            else
                trityp = 1;
        }
        else
        {
            if (trityp > 3)
                trityp = 0; //mutant
            else if ((trityp == 1) && (i + j > k))
                trityp = 2;
            else if ((trityp == 3) && (i + k > j)) //mutant
                trityp = 2;
            else if ((trityp != 3) && (j + k > i)) //mutant
                trityp = 2;
            else
                trityp = 4;
        }
    }
    return trityp;
}