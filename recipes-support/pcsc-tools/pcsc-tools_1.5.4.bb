SUMMARY = "Some tools to be used with smart cards and PC/SC"
HOME_PAGE = "http://ludovic.rousseau.free.fr/softwares/pcsc-tools"
LICENSE = "GPLv2"
SECTION = "console/tools"

SRC_URI = "http://ludovic.rousseau.free.fr/softwares/${PN}/${PN}-${PV}.tar.bz2"

SRC_URI[md5sum] = "4f4d917f5d3fda88167e2bf78cbd4c3b"
SRC_URI[sha256sum] = "c39e6abba781895220c68df5e5d2f0f9547c7a676eebec3f1ddcff8794377c93"

LIC_FILES_CHKSUM = "file://LICENSE;md5=cb901168715f4782a2b06c3ddaefa558"
LIC_FILES_CHKSUM = "file://README;beginline=111;endline=127;md5=4c621c41acd1bbbaa987b21e13fedb51"

DEPENDS = "pcsc-lite"
RDEPENDS_{PN} = "pcsc-lite-lib"

inherit autotools pkgconfig

FILES_${PN} += "/usr/share/*"
