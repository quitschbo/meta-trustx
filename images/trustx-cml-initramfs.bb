DECRIPTION = "Minimal initramfs-based root file system for CML"

PACKAGE_INSTALL = "\
	packagegroup-core-boot \
	${VIRTUAL-RUNTIME_base-utils} \
	udev \
	base-passwd \
	shadow \
	mingetty \
	libselinux \
	cmld \
	service-static \
	control \
	scd \
	iptables \
	ibmtss2 \
	tpm2d \
	rattestation \
	stunnel \
	openssl-tpm2-engine \
	e2fsprogs-mke2fs \
	e2fsprogs-e2fsck \
	btrfs-tools \
	${ROOTFS_BOOTSTRAP_INSTALL} \
	cml-boot \
	pcsc-tools \
	socat \
"

PACKAGE_INSTALL += "\
	openssl-bin \
	gptfdisk \
	parted \
	util-linux-sfdisk \
"

#PACKAGE_INSTALL += "\
#	strace \
#	kvmtool \
#"

IMAGE_LINUGUAS = " "

LICENSE = "GPLv2"

IMAGE_FEATURES = ""

export IMAGE_BASENAME = "trustx-cml-initramfs"
IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit image

IMAGE_FEATURES_remove += "package-management"

IMAGE_ROOTFS_SIZE = "4096"

KERNELVERSION="$(cat "${STAGING_KERNEL_BUILDDIR}/kernel-abiversion")"

update_fstab () {

    mkdir -p ${IMAGE_ROOTFS}/data
    cat >> ${IMAGE_ROOTFS}/etc/fstab <<EOF

tmpfs /tmp tmpfs defaults 0 0

/dev/disk/by-label/boot /boot vfat umask=0077 0 1
/dev/disk/by-label/trustme /mnt ext4 defaults 0 0
EOF
}

update_inittab () {
    echo "tty12::respawn:${base_sbindir}/mingetty --autologin root tty12" >> ${IMAGE_ROOTFS}/etc/inittab

    mkdir -p ${IMAGE_ROOTFS}/dev
    mknod -m 622 ${IMAGE_ROOTFS}/dev/console c 5 1
    mknod -m 622 ${IMAGE_ROOTFS}/dev/tty12 c 4 1
}

#TODO modsigning option in image fstype?

update_modules_dep () {
	sh -c 'cd "${IMAGE_ROOTFS}" && depmod --basedir "${IMAGE_ROOTFS}" --config "${IMAGE_ROOTFS}/etc/depmod.d" ${KERNELVERSION}'
}

update_hostname () {
    echo "trustx-cml" > ${IMAGE_ROOTFS}/etc/hostname
}

ROOTFS_POSTPROCESS_COMMAND_append = " update_fstab; "
ROOTFS_POSTPROCESS_COMMAND_append = " update_modules_dep; "
ROOTFS_POSTPROCESS_COMMAND_append = " update_hostname; "

# For debug purpose allow login if debug-tweaks is set in local.conf
ROOTFS_POSTPROCESS_COMMAND_append = '${@bb.utils.contains_any("EXTRA_IMAGE_FEATURES", [ 'debug-tweaks' ], " update_inittab ; ", "",d)}'

inherit extrausers
EXTRA_USERS_PARAMS = '${@bb.utils.contains_any("EXTRA_IMAGE_FEATURES", [ 'debug-tweaks' ], "usermod -P root root; ", "",d)}'
