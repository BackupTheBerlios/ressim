
#ifndef M3RD_HEADER
#define M3RD_HEADER

#ifdef __cplusplus
extern "C" {
#endif

  /* Get number of nodes, cells and faces */
  void m3getinfo_();

  /* Get nodes. */
  int m3getnodes_(void);

  /* Get cells. */
  int m3getcells_(void);

  /* Get faces. */
  int m3getfaces_(void);

  /* Get material names. */
  int m3getmatnames_(void);

  /* Get cell material data. */
  void m3getcellmats_(void);

  /* Get variables and subvars. */
  void m3getvars_(void);

  /* Check for slip faces and set cell slip flags.  */
  void m3getslipflag(void);

  /* Close M3.  */
  void m3stop_(void);


#ifdef __cplusplus
}
#endif

#endif
