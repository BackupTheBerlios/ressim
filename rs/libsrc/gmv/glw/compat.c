#include <X11/IntrinsicP.h>
#include <X11/StringDefs.h>
#include <GL/glx.h>
#include <GL/gl.h>
#ifdef __GLX_MOTIF
#include <Xm/PrimitiveP.h>
#include "GLwMDrawAP.h"
#else
#include "GLwDrawAP.h"
#endif

/*
extern Widget GLwCreateMDrawingArea(Widget parent, char *name, ArgList arglist,Cardinal argcount);	
Widget GLwCreateM2DrawingArea(Widget parent,char *name,ArgList arglist,Cardinal argcount)
	__attribute__ ((weak, alias("GLwCreateMDrawingArea")));
*/	
	
Widget GLwCreateM2DrawingArea(Widget parent,char *name,ArgList arglist,Cardinal argcount) {
	return (Widget) GLwCreateMDrawingArea(parent, name, arglist, argcount);
}

