DECRIPTION = "Minimal root file system for running pcscd"

include images/trustx-signing.inc

PACKAGE_INSTALL = "\
	busybox \
	ccid \
	libgcc \
	pcsc-lite \
"

IMAGE_FSTYPES = "squashfs"

IMAGE_INSTALL = ""
IMAGE_LINUGUAS = ""

LICENSE = "GPLv2"

IMAGE_FEATURES = ""

inherit image

# pcscd expects this director for its runtime socket
populate_volatile () {
	mkdir -p ${IMAGE_ROOTFS}/var/run
}
ROOTFS_POSTPROCESS_COMMAND_append = " populate_volatile; "
