DECRIPTION = "Minimal root file system for downloading and converting Docker images"

include images/gyroidos-signing.inc

PACKAGE_INSTALL = "\
	busybox \
	service-static \
	openssl-bin \
	converter \
	lighttpd \
	control \
"

IMAGE_INSTALL = ""
IMAGE_LINUGUAS = ""

LICENSE = "GPL-2.0-only"

IMAGE_FEATURES = ""
IMAGE_FSTYPES = "squashfs"

inherit image

# lighttpd expects this to be present
populate_volatile () {
	mkdir -p ${IMAGE_ROOTFS}/var/volatile/log
	mkdir -p ${IMAGE_ROOTFS}/var/volatile/tmp
}

ROOTFS_POSTPROCESS_COMMAND:append = " populate_volatile; "
