component extends="IterSelf" {
	// a struct can't become an Iterator: conversion fails, Java (String.join) is the caller
	function iterator() {
		return { a: 1 };
	}
}
