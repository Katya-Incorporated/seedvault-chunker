# Seedvault Chunker

A FastCDC implementation in Kotlin with an unusual API.

The unusual API comes from the fact that [Seedvault](https://github.com/seedvault-app/seedvault/) is getting its data
from the Android system in irregular chunks.
It can't freely read data from a stream, but needs to wait until the system allows reading a certain amount. 

Based on [Farley-Chen/fastcdc-java](https://github.com/Farley-Chen/fastcdc-java)
which is based on [iscc/fastcdc-py](https://github.com/iscc/fastcdc-py)
and [nlfiedler/fastcdc-rs](https://github.com/nlfiedler/fastcdc-rs).
