from scratch.build_full_inventory import absent_species

print(f"Total absent: {len(absent_species)}")
for sp, reason in absent_species:
    print(f"ID {sp['id']:4d}: {sp['vulgar']:25} | {sp['scientific_name']:30} ({reason})")
