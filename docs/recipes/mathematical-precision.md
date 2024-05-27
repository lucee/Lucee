<!--
{
  "title": "Mathematical Precision",
  "id": "mathematical-precision",
  "since": "6.0",
  "description": "Learn about the switch from double to BigDecimal in Lucee 6 for more precise mathematical operations. This guide provides information on how to change the default behavior if needed.",
  "keywords": [
    "CFML",
    "math",
    "precision",
    "BigDecimal",
    "Lucee",
    "Application.cfc",
    "PrecisionEvaluate"
  ]
}
-->
# Mathematical Precision

So far, Lucee has handled numbers internally as “double”, but with Lucee 6 we have switched to “BigDecimal”. This makes math operations much more precise and there is no need anymore to use the function “PrecisionEvaluate”.

Since version 6.0, all numbers Lucee uses in the runtime are by default BigDecimal based and no longer double as before. You can still change that fact in the Application.cfc as follows:

```lucee
this.preciseMath = false;
```

## System Property / Environment Variable

You can also change that behavior with the system property `-Dlucee.precise.math=false` or with the environment variable `LUCEE_PRECISE_MATH=false`.