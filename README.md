FARTPlus
========

FARTPlus is a tool used for remapping Java artifacts. It's a modified version of [ForgeAutoRenamingTool].

## Usage

To see how to use FARTPlus run:
```
java jar fartplus.jar --help 
```

Common:
```
java -jar fartplus.jar --input in.jar --output out.jar --map mappings.tsrg
```

## Capabilities

As main, FARTPlus will rename class entries (with fields, methods and parameters) inside given JAR archive.

Also JAR manifest will be transformed by updating manifest `Main-Class` entry.

See the source code for more information.


### Deducing parameters name

FARTPlus can deduce parameters name from parameter type class name based on parameter descriptor available in parent method.

```
java -jar fartplus.jar --input in.jar --output out.jar --deduce-param-names
```

The deduced parameter name will be used when the loaded name mappings (SRG's) do not provide a mapping for the given function parameter.

### Dictionary

Special dictionaries can be used to modify deducing process output and by this resulting parameter names.

```
Dictionary rule format:
    PATTERN:FILTER ACTION:VALUE     # means do ACTION when PATTERN and FILTER matches

or shorter forms like:
    PATTERN VALUE                   # means replace
    PATTERN ACTION:VALUE            # means do ACTION
    PATTERN:FILTER VALUE            # means replace with package filter

where:
    PATTERN regex_expression 
        Regular expression to match parameter type class name
    FILTER regex_expression
        Regular expression to match parameter type class package
    ACTION one of [RENAME, PREFIX, SUFFIX, FIRST, LAST]
        Action is type of activity to do when rule matches pattern and filter
    VALUE string
        Used in RENAME, SUFFIX and PREFIX action types
    # comment
        Just comment, ignored by process
```

Example dictionary file:
```
#
# Replace primitives
#
^Z$ flag       # boolean
^C$ c          # char
^B$ b          # byte
^S$ i          # short
^I$ i          # int
^J$ i          # long
^F$ f          # float
^D$ d          # double
^V$ nothing    # void
^String$ s     # Convert long 'string' to just 's'
```

Use dictionary while deducing:

```
java -jar fartplus.jar --input in.jar --output out.jar --deduce-param-names --dictionary dictionary.dict
```
