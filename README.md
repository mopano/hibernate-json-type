
This branch serves as a proof of concept for the extensibility of the Hibernate
Array type contributor at https://github.com/mopano/hibernate-array-type.

It is NOT to be published in any Maven repositories.

How it works
-----

Inside `com.mopano.hibernate.json.JsonTypeContributor` you have this effective code,
once you strip away the use of reflection (in case the dependency is optional and not included):

    import com.mopano.hibernate.array.ArrayTypes;

    ...

    ArrayTypes JSON = ArrayTypes.get(jtype, serviceRegistry);
    JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(JSON.getJavaTypeDescriptor());
    typeContributions.contributeType(JSON);

We use `ArrayTypes.get` instead of `new ArrayTypes()`, because if the type which we
want to extend already exists, we want to return it, instead of creating a new one.
Keep in mind, that cache uses an IdentityHashMap, to be certain we are operating on
individual instances of given types, and not some potentially matching overridden
hashes.
