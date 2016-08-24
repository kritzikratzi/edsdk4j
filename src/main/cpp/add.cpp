/**
 * example file to be used with JNA
 */
#include "add.h"
extern "C" int addNumber(int a,int b)
{ 
    return a+b;
}